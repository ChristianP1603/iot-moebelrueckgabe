import ScanHistory from "./ScanHistory.jsx";
import { typLabel } from "../constants.js";

const API = "/api";

const ZUSTAND_LABELS = {
  GUT: "Gut",
  DEFEKT: "Defekt",
  IN_REPARATUR: "In Reparatur",
  ENTSORGT: "Entsorgt",
  TEILWEISE_BESCHAEDIGT: "Teilweise beschädigt",
};

const ZUSTAND_COLORS = {
  GUT: "#4caf50",
  DEFEKT: "#ff9800",
  IN_REPARATUR: "#f44336",
  ENTSORGT: "#9e9e9e",
  TEILWEISE_BESCHAEDIGT: "#607d8b",
};

export default function MoebelPopup({ moebel, scans, loading }) {
  const farbe = ZUSTAND_COLORS[moebel.zustand] || "#999";
  const label = ZUSTAND_LABELS[moebel.zustand] || moebel.zustand;

  return (
    <div style={{ minWidth: "240px", maxWidth: "300px", fontSize: "0.88rem" }}>
      <div style={{ fontWeight: 700, fontSize: "1rem", marginBottom: "4px" }}>
        {moebel.bezeichnung}
      </div>
      <div style={{ marginBottom: "8px" }}>
        <span style={{
          display: "inline-block", padding: "2px 8px", borderRadius: "12px",
          background: farbe, color: "white", fontSize: "0.78rem", fontWeight: 600,
        }}>
          {label}
        </span>
        <span style={{ marginLeft: "8px", color: "#666" }}>{typLabel(moebel.typ)}</span>
      </div>

      {moebel.hat_foto && (
        <div style={{ marginBottom: "8px" }}>
          <img
            src={`${API}/moebelstuck/${moebel.id}/foto`}
            alt={moebel.bezeichnung}
            style={{ width: "100%", borderRadius: "6px", maxHeight: "160px", objectFit: "cover" }}
          />
        </div>
      )}

      <table style={{ width: "100%", borderCollapse: "collapse", marginBottom: "10px" }}>
        <tbody>
          {moebel.standort_name && (
            <tr>
              <td style={{ color: "#888", paddingRight: "8px" }}>Standort</td>
              <td>{moebel.standort_name}</td>
            </tr>
          )}
          {moebel.preis && (
            <tr>
              <td style={{ color: "#888", paddingRight: "8px" }}>Preis</td>
              <td>{Number(moebel.preis).toFixed(2)} €</td>
            </tr>
          )}
          {moebel.kaufdatum && (
            <tr>
              <td style={{ color: "#888", paddingRight: "8px" }}>Kaufdatum</td>
              <td>{new Date(moebel.kaufdatum).toLocaleDateString("de-AT")}</td>
            </tr>
          )}
          <tr>
            <td style={{ color: "#888", paddingRight: "8px" }}>Tag-ID</td>
            <td style={{ fontFamily: "monospace", fontSize: "0.8rem" }}>{moebel.nfc_tag_id}</td>
          </tr>
        </tbody>
      </table>

      <div style={{ fontWeight: 600, marginBottom: "6px", borderTop: "1px solid #eee", paddingTop: "8px" }}>
        Bewegungshistorie
      </div>
      {loading ? (
        <p style={{ color: "#999", fontSize: "0.82rem" }}>Wird geladen…</p>
      ) : (
        <ScanHistory scans={scans} />
      )}
    </div>
  );
}
