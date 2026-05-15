import { useEffect, useState, useCallback, useRef } from "react";
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap, ZoomControl } from "react-leaflet";
import L from "leaflet";
import MoebelPopup from "../components/MoebelPopup.jsx";

const API = "/api";

const ZUSTAND_FARBEN = {
  GUT: "#4caf50",
  DEFEKT: "#ff9800",
  IN_REPARATUR: "#f44336",
  ENTSORGT: "#9e9e9e",
  TEILWEISE_BESCHAEDIGT: "#607d8b",
};

function makeIcon(farbe) {
  return L.divIcon({
    className: "",
    html: `<div style="
      width:28px;height:28px;border-radius:50%;
      background:${farbe};border:3px solid white;
      box-shadow:0 2px 6px rgba(0,0,0,0.4);
    "></div>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14],
    popupAnchor: [0, -16],
  });
}

function MapReady() {
  const map = useMap();
  useEffect(() => {
    setTimeout(() => map.invalidateSize(), 200);
  }, []);
  return null;
}

function FitBounds({ positions }) {
  const map = useMap();
  const hasFitted = useRef(false);
  useEffect(() => {
    if (positions.length > 0 && !hasFitted.current) {
      setTimeout(() => {
        map.invalidateSize();
        map.fitBounds(positions, { padding: [40, 40], maxZoom: 17 });
      }, 300);
      hasFitted.current = true;
    }
  }, [positions.length]);
  return null;
}

export default function MapPage() {
  const [moebel, setMoebel] = useState([]);
  const [scanCache, setScanCache] = useState({});
  const [loadingId, setLoadingId] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [error, setError] = useState(null);
  const [filterTyp, setFilterTyp] = useState("");
  const [filterZustand, setFilterZustand] = useState("");

  useEffect(() => {
    function loadData() {
      fetch(`${API}/moebelstuck`)
        .then((r) => r.json())
        .then(setMoebel)
        .catch(() => setError("Backend nicht erreichbar."));
    }
    loadData();
    const interval = setInterval(loadData, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleSelect = useCallback(async (m) => {
    setSelectedId(m.id);
    setLoadingId(m.id);
    try {
      const res = await fetch(`${API}/moebelstuck/${m.id}`);
      const data = await res.json();
      setScanCache((prev) => ({ ...prev, [m.id]: data.scan_history || [] }));
    } catch {
      setScanCache((prev) => ({ ...prev, [m.id]: [] }));
    } finally {
      setLoadingId(null);
    }
  }, []);

  const typen = [...new Set(moebel.map((m) => m.typ))].sort();

  const filtered = moebel.filter((m) => {
    if (filterTyp && m.typ !== filterTyp) return false;
    if (filterZustand && m.zustand !== filterZustand) return false;
    return true;
  });

  const withCoords = filtered.filter((m) => m.standort_lat && m.standort_lng);
  const bounds = withCoords.map((m) => [m.standort_lat, m.standort_lng]);

  const selectedScans = selectedId ? (scanCache[selectedId] || []) : [];
  const selectedPath = selectedScans
    .filter((s) => s.standort_lat && s.standort_lng)
    .map((s) => [s.standort_lat, s.standort_lng]);

  return (
    <div style={{ position: "relative" }}>
      {error && (
        <div style={{
          position: "absolute", top: 10, left: "50%", transform: "translateX(-50%)",
          zIndex: 1000, background: "#f44336", color: "white",
          padding: "8px 16px", borderRadius: "6px", fontSize: "0.9rem",
        }}>
          {error}
        </div>
      )}

      <div style={{
        position: "absolute", bottom: 30, right: "calc(10px + env(safe-area-inset-right))", zIndex: 1000,
        background: "white", borderRadius: "8px", padding: "10px 14px",
        boxShadow: "0 2px 8px rgba(0,0,0,0.15)", fontSize: "0.8rem",
      }}>
        <div style={{ fontWeight: 600, marginBottom: "6px" }}>Filter</div>
        <select
          value={filterTyp}
          onChange={(e) => setFilterTyp(e.target.value)}
          style={{ width: "100%", padding: "4px 6px", borderRadius: "4px", border: "1px solid #ccc", fontSize: "0.8rem", marginBottom: "6px" }}
        >
          <option value="">Alle Typen</option>
          {typen.map((t) => (
            <option key={t} value={t}>{t}</option>
          ))}
        </select>
        <select
          value={filterZustand}
          onChange={(e) => setFilterZustand(e.target.value)}
          style={{ width: "100%", padding: "4px 6px", borderRadius: "4px", border: "1px solid #ccc", fontSize: "0.8rem" }}
        >
          <option value="">Alle Zustaende</option>
          {Object.keys(ZUSTAND_FARBEN).map((z) => (
            <option key={z} value={z}>{z.replace(/_/g, " ")}</option>
          ))}
        </select>
        <div style={{ marginTop: "6px", color: "#999" }}>{withCoords.length} / {moebel.length} angezeigt</div>
      </div>

      <div style={{
        position: "fixed", top: "calc(52px + env(safe-area-inset-top) + 10px)", left: "calc(10px + env(safe-area-inset-left))", zIndex: 1000,
        background: "white", borderRadius: "8px", padding: "10px 14px",
        boxShadow: "0 2px 8px rgba(0,0,0,0.15)", fontSize: "0.8rem",
      }}>
        <div style={{ fontWeight: 600, marginBottom: "6px" }}>Legende</div>
        {Object.entries(ZUSTAND_FARBEN).map(([z, f]) => (
          <div key={z} style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "3px" }}>
            <div style={{ width: 12, height: 12, borderRadius: "50%", background: f, flexShrink: 0 }} />
            <span style={{ textTransform: "capitalize" }}>{z.replace(/_/g, " ")}</span>
          </div>
        ))}
      </div>

      <MapContainer
        center={[48.336, 14.32]}
        zoom={15}
        zoomControl={false}
        style={{ height: "calc(100vh - 52px - env(safe-area-inset-top))", width: "100%" }}
      >
        <MapReady />
        <ZoomControl position="bottomleft" />
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {bounds.length > 0 && <FitBounds positions={bounds} />}

        {withCoords.map((m) => (
          <Marker
            key={m.id}
            position={[m.standort_lat, m.standort_lng]}
            icon={makeIcon(ZUSTAND_FARBEN[m.zustand] || "#999")}
            eventHandlers={{ click: () => handleSelect(m) }}
          >
            <Popup maxWidth={320} minWidth={240}>
              <MoebelPopup
                moebel={m}
                scans={scanCache[m.id] || []}
                loading={loadingId === m.id}
              />
            </Popup>
          </Marker>
        ))}

        {selectedId && selectedPath.length > 1 && (
          <Polyline
            positions={selectedPath}
            color="#2196f3"
            weight={2}
            dashArray="5,5"
          />
        )}
      </MapContainer>
    </div>
  );
}
