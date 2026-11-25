import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import {
  ArrowRightFromLine,
  BriefcaseBusiness,
  Menu,
  Network,
  Search,
  UserRound,
  UsersRound,
  X,
} from "lucide-react";
import { useAuth } from "../../auth/useAuth";

const navItems = [
  { to: "/profile", label: "Profile", icon: UserRound },
  { to: "/search", label: "Discover", icon: Search },
  { to: "/connections", label: "Connections", icon: UsersRound },
  { to: "/referrals", label: "Referrals", icon: BriefcaseBusiness },
];

const Navigation = ({ onNavigate }) => (
  <nav className="nav-list" aria-label="Primary navigation">
    {navItems.map(({ to, label, icon: Icon }) => (
      <NavLink
        key={to}
        to={to}
        onClick={onNavigate}
        className={({ isActive }) => `nav-item${isActive ? " nav-item-active" : ""}`}
      >
        <Icon size={19} strokeWidth={1.9} />
        <span>{label}</span>
      </NavLink>
    ))}
  </nav>
);

const Brand = () => (
  <div className="brand-lockup">
    <span className="brand-mark" aria-hidden="true">
      <Network size={20} strokeWidth={2.2} />
    </span>
    <span>
      <strong>Relay</strong>
      <small>Referral network</small>
    </span>
  </div>
);

const Navbar = () => {
  const [open, setOpen] = useState(false);
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <>
      <header className="mobile-header">
        <Brand />
        <button
          type="button"
          className="icon-button"
          onClick={() => setOpen(true)}
          aria-label="Open navigation"
        >
          <Menu size={21} />
        </button>
      </header>

      {open && (
        <button
          type="button"
          className="nav-scrim"
          aria-label="Close navigation"
          onClick={() => setOpen(false)}
        />
      )}

      <aside className={`sidebar${open ? " sidebar-open" : ""}`)}>
        <div className="sidebar-top">
          <Brand />
          <button
            type="button"
            className="icon-button sidebar-close"
            onClick={() => setOpen(false)}
            aria-label="Close navigation"
          >
            <X size={20} />
          </button>
        </div>

        <div className="sidebar-label">Workspace</div>
        <Navigation onNavigate={() => setOpen(false)} />

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <span className="avatar avatar-coral">R</span>
            <span>
              <strong>Your workspace</strong>
              <small>Professional account</small>
            </span>
          </div>
          <button type="button" className="logout-button" onClick={handleLogout}>
            <ArrowRightFromLine size={18} />
            <span>Sign out</span>
          </button>
        </div>
      </aside>
    </>
  );
};

export default Navbar;
