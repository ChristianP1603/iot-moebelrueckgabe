import { useEffect, useState, useCallback, useRef } from "react";
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap, ZoomControl } from "react-leaflet";
import L from "leaflet";
import MoebelPopup from "../components/MoebelPopup.jsx";
import { typLabel } from "../constants.js";

const API = "/api";

const TYP_FARBEN = {
  SCHREIBTISCH: "#1565c0",
  STUHL: "#2e7d32",
  TISCH: "#6a1b9a",
  SCHRANK: "#e65100",
  ROLLCONTAINER: "#00838f",
  REGAL: "#4e342e",
  LAMPE: "#f9a825",
  WHITEBOARD: "#546e7a",
  SOFA: "#c62828",
  GARDEROBE: "#ad1457",
  SESSEL: "#1b5e20",
  STEHPULT: "#0277bd",
  KONFERENZTISCH: "#4527a0",
  AKTENSCHRANK: "#bf360c",
  SIDEBOARD: "#ff6f00",
  KOMMODE: "#795548",
  VITRINE: "#00695c",
  FLIPCHART: "#37474f",
  PINNWAND: "#880e4f",
  BETT: "#5d4037",
  MATRATZE: "#827717",
  COUCHTISCH: "#311b92",
  SONSTIGES: "#757575",
};

const iconCache = {};

function getIconForTyp(typ) {
  if (!iconCache[typ]) {
    const farbe = TYP_FARBEN[typ] || "#757575";
    iconCache[typ] = L.divIcon({
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
  return iconCache[typ];
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
            <option key={t} value={t}>{typLabel(t)}</option>
          ))}
        </select>
        <div style={{ marginTop: "6px", color: "#999" }}>{withCoords.length} / {moebel.length} angezeigt</div>
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
            icon={getIconForTyp(m.typ)}
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
