import { useEffect, useState } from "react";

const API = "/api";

const ZUSTAND_FARBEN = {
  GUT: "#4caf50",
  DEFEKT: "#ff9800",
  IN_REPARATUR: "#f44336",
  ENTSORGT: "#9e9e9e",
  TEILWEISE_BESCHAEDIGT: "#607d8b",
};

export default function AdminPage() {
  const [moebel, setMoebel] = useState([]);
  const [loading, setLoading] = useState(true);
  const [resetting, setResetting] = useState(null);
  const [message, setMessage] = useState(null);

  async function loadMoebel() {
    try {
      const res = await fetch(`${API}/moebelstuck`);
      setMoebel(await res.json());
    } catch {
      setMessage("Backend nicht erreichbar.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadMoebel(); }, []);

  async function resetEinzeln(id) {
    setResetting(id);
    setMessage(null);
    try {
      const res = await fetch(`${API}/moebelstuck/${id}/reset`, { method: "POST" });
      if (!res.ok) throw new Error();
      await loadMoebel();
      setMessage("Möbelstück zurückgesetzt.");
    } catch {
      setMessage("Fehler beim Zurücksetzen.");
    } finally {
      setResetting(null);
    }
  }

  async function resetAlle() {
    if (!confirm("Alle Möbelstücke auf Standardwerte zurücksetzen?")) return;
    setResetting("all");
    setMessage(null);
    try {
      const res = await fetch(`${API}/moebelstuck/reset`, { method: "POST" });
      if (!res.ok) throw new Error();
      const data = await res.json();
      await loadMoebel();
      setMessage(`${data.count} Möbelstücke zurückgesetzt.`);
    } catch {
      setMessage("Fehler beim Zurücksetzen.");
    } finally {
      setResetting(null);
    }
  }

  if (loading) {
    return <div style={{ padding: "40px", textAlign: "center", color: "#666" }}>Wird geladen...</div>;
  }

  return (
    <div style={{ maxWidth: "600px", margin: "24px auto", padding: "0 16px", paddingBottom: "calc(20px + env(safe-area-inset-bottom))" }}>
      <div style={card}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
          <div style={{ fontWeight: 700, fontSize: "1.05rem" }}>Möbelstücke verwalten</div>
          <button
            onClick={resetAlle}
            disabled={resetting === "all"}
            style={{
              padding: "10px 16px", borderRadius: "8px",
              border: "none", background: resetting === "all" ? "#ccc" : "#d32f2f",
              color: "white", fontWeight: 600, fontSize: "0.85rem",
              cursor: resetting === "all" ? "not-allowed" : "pointer",
              minHeight: "44px",
            }}
          >
            {resetting === "all" ? "Wird zurückgesetzt..." : "Alle zurücksetzen"}
          </button>
        </div>

        {message && (
          <div style={{
            padding: "10px 14px", borderRadius: "6px", marginBottom: "14px",
            background: message.includes("Fehler") ? "#ffebee" : "#e8f5e9",
            color: message.includes("Fehler") ? "#c62828" : "#2e7d32",
            fontSize: "0.85rem",
          }}>
            {message}
          </div>
        )}

        {moebel.map((m) => (
          <div key={m.id} style={{
            display: "flex", justifyContent: "space-between", alignItems: "center",
            padding: "12px 0", borderBottom: "1px solid #eee",
          }}>
            <div>
              <div style={{ fontWeight: 600, fontSize: "0.9rem" }}>{m.bezeichnung}</div>
              <div style={{ fontSize: "0.8rem", color: "#888", marginTop: "2px" }}>
                <span style={{ fontFamily: "monospace" }}>{m.nfc_tag_id}</span>
                <span style={{ margin: "0 6px" }}>·</span>
                <span style={{ color: ZUSTAND_FARBEN[m.zustand] || "#999", fontWeight: 600 }}>
                  {m.zustand?.replace("_", " ")}
                </span>
                {m.standort_name && (
                  <>
                    <span style={{ margin: "0 6px" }}>·</span>
                    <span>{m.standort_name}</span>
                  </>
                )}
              </div>
            </div>
            <button
              onClick={() => resetEinzeln(m.id)}
              disabled={resetting === m.id}
              style={{
                padding: "8px 14px", borderRadius: "6px",
                border: "1px solid #ddd", background: "white",
                color: "#333", fontSize: "0.8rem", fontWeight: 600,
                cursor: resetting === m.id ? "not-allowed" : "pointer",
                minHeight: "40px", whiteSpace: "nowrap",
              }}
            >
              {resetting === m.id ? "..." : "Zurücksetzen"}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

const card = {
  background: "white",
  borderRadius: "12px",
  padding: "20px",
  boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
};
