import { useEffect, useState, useRef } from "react";
import { useSearchParams } from "react-router-dom";
import { typLabel } from "../constants.js";

const API = "/api";

const EVENT_OPTIONEN = [
  { value: "RUECKGABE", label: "Rückgabe" },
  { value: "PRUEFUNG", label: "Prüfung" },
  { value: "EINLAGERUNG", label: "Einlagerung" },
  { value: "REPARATUR", label: "Reparatur" },
  { value: "TEILDEMONTAGE", label: "Teildemontage" },
  { value: "ENTSORGUNG", label: "Entsorgung" },
];

const PRUEFERGEBNIS_OPTIONEN = [
  { value: "GUT", label: "Guter Zustand" },
  { value: "REPARATUR", label: "Reparatur nötig" },
  { value: "TEILWEISE_BESCHAEDIGT", label: "Teilweise beschädigt" },
  { value: "SCHLECHT", label: "Nicht reparierbar" },
];

const ZUSTAND_FARBEN = {
  GUT: "#4caf50",
  DEFEKT: "#ff9800",
  IN_REPARATUR: "#f44336",
  ENTSORGT: "#9e9e9e",
  TEILWEISE_BESCHAEDIGT: "#607d8b",
};

export default function ScanPage() {
  const [params] = useSearchParams();
  const tagIdFromUrl = params.get("id") || "";

  const [manualTagId, setManualTagId] = useState("");
  const [moebel, setMoebel] = useState(null);
  const [tagUnbekannt, setTagUnbekannt] = useState(false);
  const [loading, setLoading] = useState(!!tagIdFromUrl);

  const [eventTyp, setEventTyp] = useState("RUECKGABE");
  const [standortName, setStandortName] = useState("");
  const [gescanntVon, setGescanntVon] = useState("");
  const [gps, setGps] = useState({ lat: null, lng: null });
  const [gpsStatus, setGpsStatus] = useState("ausstehend");
  const [foto, setFoto] = useState(null);
  const [fotoPreview, setFotoPreview] = useState(null);
  const fileInputRef = useRef(null);
  const [pruefergebnis, setPruefergebnis] = useState("GUT");
  const [ersatzteileVorhanden, setErsatzteileVorhanden] = useState(false);

  const [submitting, setSubmitting] = useState(false);
  const [erfolg, setErfolg] = useState(null);
  const [fehler, setFehler] = useState(null);

  const activeTagId = tagIdFromUrl || manualTagId.trim();

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setGps({ lat: pos.coords.latitude, lng: pos.coords.longitude });
          setGpsStatus("erfasst");
        },
        () => setGpsStatus("verweigert")
      );
    } else {
      setGpsStatus("nicht unterstützt");
    }
  }, []);

  useEffect(() => {
    if (!tagIdFromUrl) return;
    lookupTag(tagIdFromUrl);
  }, [tagIdFromUrl]);

  function lookupTag(tagId) {
    setLoading(true);
    setTagUnbekannt(false);
    setMoebel(null);
    setFehler(null);
    fetch(`${API}/scan/${tagId}`)
      .then((r) => r.json())
      .then((data) => {
        if (data.status === "unbekannt") {
          setTagUnbekannt(true);
        } else {
          setMoebel(data);
        }
      })
      .catch(() => setFehler("Backend nicht erreichbar."))
      .finally(() => setLoading(false));
  }

  function handleManualLookup(e) {
    e.preventDefault();
    if (!manualTagId.trim()) return;
    lookupTag(manualTagId.trim());
  }

  function handleFotoChange(e) {
    const file = e.target.files[0];
    if (!file) return;
    setFoto(file);
    const reader = new FileReader();
    reader.onload = (ev) => setFotoPreview(ev.target.result);
    reader.readAsDataURL(file);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!standortName.trim() || !activeTagId) return;
    setSubmitting(true);
    setFehler(null);
    try {
      const res = await fetch(`${API}/scan`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nfc_tag_id: activeTagId,
          standort_lat: gps.lat,
          standort_lng: gps.lng,
          standort_name: standortName.trim(),
          event_typ: eventTyp,
          gescannt_von: gescanntVon.trim() || null,
          pruefergebnis: eventTyp === "PRUEFUNG" ? pruefergebnis : null,
          ersatzteile_vorhanden: eventTyp === "REPARATUR" ? ersatzteileVorhanden : null,
        }),
      });
      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || err.detail || "Fehler beim Scannen");
      }
      const data = await res.json();

      if (foto && data.id) {
        const formData = new FormData();
        formData.append("file", foto);
        await fetch(`${API}/moebelstuck/${data.id}/foto`, {
          method: "POST",
          body: formData,
        });
      }

      setErfolg(data);
    } catch (err) {
      setFehler(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  const card = {
    background: "white",
    borderRadius: "12px",
    padding: "20px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
    marginBottom: "16px",
  };

  if (loading) {
    return (
      <div style={{ padding: "40px", textAlign: "center", color: "#666" }}>
        Tag wird geladen…
      </div>
    );
  }

  if (tagUnbekannt) {
    return (
      <div style={{ maxWidth: "480px", margin: "40px auto", padding: "0 16px" }}>
        <div style={{ ...card, textAlign: "center", color: "#f44336" }}>
          <div style={{ fontSize: "2rem", marginBottom: "8px" }}>Fehler:</div>
          <div style={{ fontWeight: 600 }}>Dieser Tag ist noch nicht im System angelegt</div>
          <div style={{ fontSize: "0.85rem", color: "#999", marginTop: "4px" }}>Tag-ID: {activeTagId}</div>
        </div>
      </div>
    );
  }

  if (erfolg) {
    return (
      <div style={{ maxWidth: "480px", margin: "40px auto", padding: "0 16px" }}>
        <div style={{ ...card, textAlign: "center", color: "#4caf50" }}>
          <div style={{ fontSize: "2.5rem", marginBottom: "8px" }}>Erfolg</div>
          <div style={{ fontWeight: 700, fontSize: "1.1rem" }}>Scan erfolgreich</div>
          <div style={{ marginTop: "12px", fontSize: "0.9rem", color: "#555" }}>
            <strong>{erfolg.bezeichnung}</strong> wurde als <strong>{eventTyp}</strong> registriert.
          </div>
          <div style={{ fontSize: "0.85rem", color: "#999", marginTop: "4px" }}>Standort: {erfolg.standort_name}</div>
          <button
            onClick={() => { setErfolg(null); setStandortName(""); setFoto(null); setFotoPreview(null); setPruefergebnis("GUT"); setErsatzteileVorhanden(false); }}
            style={{
              marginTop: "20px", padding: "14px 28px", borderRadius: "10px",
              border: "none", background: "#2196f3", color: "white",
              cursor: "pointer", fontWeight: 600, minHeight: "48px",
              fontSize: "1rem", WebkitAppearance: "none",
            }}
          >
            Weiteren Scan durchführen
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: "480px", margin: "24px auto", padding: "0 16px", paddingBottom: "calc(20px + env(safe-area-inset-bottom))" }}>
      {!tagIdFromUrl && !moebel && (
        <form onSubmit={handleManualLookup} style={card}>
          <div style={{ fontWeight: 600, marginBottom: "10px" }}>Manueller Scan</div>
          <label style={labelStyle}>NFC Tag-ID eingeben</label>
          <div style={{ display: "flex", gap: "8px" }}>
            <input
              type="text"
              placeholder="z.B. MOEBL-0001"
              value={manualTagId}
              onChange={(e) => setManualTagId(e.target.value)}
              style={{ ...inputStyle, flex: 1, fontFamily: "monospace", marginBottom: 0, fontSize: "16px" }}
            />
            <button
              type="submit"
              disabled={!manualTagId.trim()}
              style={{
                padding: "12px 18px", borderRadius: "8px",
                border: "none", background: "#1565c0", color: "white",
                fontWeight: 600, cursor: manualTagId.trim() ? "pointer" : "not-allowed",
                whiteSpace: "nowrap", minHeight: "48px",
                fontSize: "1rem", WebkitAppearance: "none",
              }}
            >
              Suchen
            </button>
          </div>
        </form>
      )}

      {moebel && (
        <div style={card}>
          <div style={{ fontWeight: 700, fontSize: "1.05rem", marginBottom: "4px" }}>
            {moebel.bezeichnung}
          </div>
          <span style={{
            display: "inline-block", padding: "2px 10px", borderRadius: "12px",
            background: ZUSTAND_FARBEN[moebel.zustand] || "#999",
            color: "white", fontSize: "0.8rem", fontWeight: 600, marginBottom: "10px",
          }}>
            {moebel.zustand?.replace(/_/g, " ")}
          </span>
          <table style={{ width: "100%", fontSize: "0.85rem", borderCollapse: "collapse" }}>
            <tbody>
              <tr><td style={{ color: "#888", paddingRight: "8px" }}>Typ</td><td>{typLabel(moebel.typ)}</td></tr>
              <tr><td style={{ color: "#888" }}>Tag-ID</td><td style={{ fontFamily: "monospace" }}>{moebel.nfc_tag_id}</td></tr>
              {moebel.standort_name && <tr><td style={{ color: "#888" }}>Letzter Standort</td><td>{moebel.standort_name}</td></tr>}
              {moebel.preis && <tr><td style={{ color: "#888" }}>Preis</td><td>{Number(moebel.preis).toFixed(2)} €</td></tr>}
              {moebel.kaufdatum && <tr><td style={{ color: "#888" }}>Kaufdatum</td><td>{new Date(moebel.kaufdatum).toLocaleDateString("de-AT")}</td></tr>}
            </tbody>
          </table>
        </div>
      )}

      {moebel && (
        <form onSubmit={handleSubmit}>
          <div style={card}>
            <div style={{ fontWeight: 600, marginBottom: "14px" }}>Scan bestätigen</div>

            <label style={labelStyle}>Ereignis</label>
            <select
              value={eventTyp}
              onChange={(e) => setEventTyp(e.target.value)}
              style={inputStyle}
            >
              {EVENT_OPTIONEN.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>

            <label style={labelStyle}>Standortname *</label>
            <input
              required
              type="text"
              placeholder="z.B. Lager A, Büro 203"
              value={standortName}
              onChange={(e) => setStandortName(e.target.value)}
              style={inputStyle}
            />

            <label style={labelStyle}>Gescannt von</label>
            <input
              type="text"
              placeholder="Name (optional)"
              value={gescanntVon}
              onChange={(e) => setGescanntVon(e.target.value)}
              style={inputStyle}
            />

            {eventTyp === "RUECKGABE" && (
              <>
                <label style={labelStyle}>Foto des Möbelstücks</label>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  capture="environment"
                  onChange={handleFotoChange}
                  style={{ display: "none" }}
                />
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  style={{
                    width: "100%", padding: "12px", borderRadius: "8px",
                    border: "2px dashed #ccc", background: fotoPreview ? "#e8f5e9" : "#fafafa",
                    color: "#555", fontSize: "0.9rem", cursor: "pointer",
                    marginBottom: "14px", minHeight: "48px",
                  }}
                >
                  {fotoPreview ? "Foto aufgenommen – zum Ändern tippen" : "Foto aufnehmen oder auswählen"}
                </button>
                {fotoPreview && (
                  <div style={{ marginBottom: "14px" }}>
                    <img
                      src={fotoPreview}
                      alt="Vorschau"
                      style={{ width: "100%", borderRadius: "8px", maxHeight: "200px", objectFit: "cover" }}
                    />
                  </div>
                )}
              </>
            )}

            {eventTyp === "PRUEFUNG" && (
              <>
                <label style={labelStyle}>Prüfergebnis *</label>
                <select
                  value={pruefergebnis}
                  onChange={(e) => setPruefergebnis(e.target.value)}
                  style={inputStyle}
                >
                  {PRUEFERGEBNIS_OPTIONEN.map((o) => (
                    <option key={o.value} value={o.value}>{o.label}</option>
                  ))}
                </select>
              </>
            )}

            {eventTyp === "REPARATUR" && (
              <label style={{
                display: "flex", alignItems: "center", gap: "10px",
                fontSize: "0.9rem", fontWeight: 600, color: "#555",
                marginBottom: "14px", cursor: "pointer",
              }}>
                <input
                  type="checkbox"
                  checked={ersatzteileVorhanden}
                  onChange={(e) => setErsatzteileVorhanden(e.target.checked)}
                  style={{ width: "20px", height: "20px", accentColor: "#1565c0" }}
                />
                Ersatzteile vorhanden
              </label>
            )}

            <div style={{ fontSize: "0.78rem", color: gpsStatus === "erfasst" ? "#4caf50" : "#999", marginBottom: "16px" }}>
              GPS: {gpsStatus === "erfasst"
                ? `${gps.lat?.toFixed(5)}, ${gps.lng?.toFixed(5)}`
                : gpsStatus === "verweigert"
                ? "Berechtigung verweigert - Standort wird ohne GPS gespeichert"
                : gpsStatus}
            </div>

            {fehler && (
              <div style={{ color: "#f44336", fontSize: "0.85rem", marginBottom: "12px" }}>
                Fehler: {fehler}
              </div>
            )}

            <button
              type="submit"
              disabled={submitting || !standortName.trim()}
              style={{
                width: "100%", padding: "14px", borderRadius: "10px",
                border: "none", background: submitting ? "#90caf9" : "#1565c0",
                color: "white", fontWeight: 700, fontSize: "1rem",
                cursor: submitting ? "not-allowed" : "pointer",
                minHeight: "48px", WebkitAppearance: "none",
              }}
            >
              {submitting ? "Wird gespeichert…" : "Scan bestätigen"}
            </button>
          </div>
        </form>
      )}

      {!moebel && tagIdFromUrl && fehler && (
        <div style={{ ...card, color: "#f44336", textAlign: "center" }}>Fehler: {fehler}</div>
      )}
    </div>
  );
}

const labelStyle = {
  display: "block",
  fontSize: "0.85rem",
  fontWeight: 600,
  color: "#555",
  marginBottom: "6px",
};

const inputStyle = {
  width: "100%",
  padding: "12px 14px",
  borderRadius: "8px",
  border: "1px solid #ddd",
  fontSize: "16px",
  marginBottom: "14px",
  outline: "none",
  minHeight: "44px",
  WebkitAppearance: "none",
};
