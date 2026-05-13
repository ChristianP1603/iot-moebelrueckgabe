import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import MapPage from "./pages/MapPage.jsx";
import ScanPage from "./pages/ScanPage.jsx";
import AdminPage from "./pages/AdminPage.jsx";

export default function App() {
  return (
    <BrowserRouter>
      <header className="app-header">
        <h1>JKU Möbelrückgabe</h1>
        <nav>
          <Link to="/">Karte</Link>
          <Link to="/scan">Scan</Link>
          <Link to="/admin">Admin</Link>
        </nav>
      </header>
      <Routes>
        <Route path="/" element={<MapPage />} />
        <Route path="/scan" element={<ScanPage />} />
        <Route path="/admin" element={<AdminPage />} />
      </Routes>
    </BrowserRouter>
  );
}
