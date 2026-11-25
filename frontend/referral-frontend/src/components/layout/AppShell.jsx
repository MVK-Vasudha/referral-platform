import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";

const AppShell = () => (
  <div className="app-shell">
    <Navbar />
    <main className="app-main">
      <Outlet />
    </main>
  </div>
);

export default AppShell;
