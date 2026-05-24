/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { AdminAccount, SecurityConfig } from "../types";
import { UserPlus, X, Trash2, ShieldCheck, ShieldAlert, CheckCircle, AlertTriangle } from "lucide-react";

interface SettingsViewProps {
  admins: AdminAccount[];
  config: SecurityConfig;
  onAddAdmin: (admin: { name: string; email: string; role: 'Super Admin' | 'Analyst' }) => void;
  onUpdateConfig: (config: Partial<SecurityConfig>) => void;
}

export default function SettingsView({
  admins,
  config,
  onAddAdmin,
  onUpdateConfig
}: SettingsViewProps) {
  const [require2FA, setRequire2FA] = useState(config.require2FA);
  const [sessionTimeout, setSessionTimeout] = useState(config.sessionTimeout);
  const [maxLoginAttempts, setMaxLoginAttempts] = useState(config.maxLoginAttempts);

  // Composing admin account form state
  const [newAdminName, setNewAdminName] = useState("");
  const [newAdminEmail, setNewAdminEmail] = useState("");
  const [newAdminRole, setNewAdminRole] = useState<'Super Admin' | 'Analyst'>("Analyst");
  const [showAddForm, setShowAddForm] = useState(false);

  // Beautiful custom modal/notification states for iframe compatibility and a high-tier UI
  const [showSuccessToast, setShowSuccessToast] = useState(false);
  const [toastMessage, setToastMessage] = useState("");
  const [showSelfDestructModal, setShowSelfDestructModal] = useState(false);
  const [selfDestructConfirmText, setSelfDestructConfirmText] = useState("");
  const [panicDeploySuccess, setPanicDeploySuccess] = useState(false);

  const triggerToast = (message: string) => {
    setToastMessage(message);
    setShowSuccessToast(true);
    setTimeout(() => {
      setShowSuccessToast(false);
    }, 4000);
  };

  // Trigger Policy Change Sync
  const handleSavePolicy = () => {
    onUpdateConfig({
      require2FA,
      sessionTimeout: Number(sessionTimeout),
      maxLoginAttempts: Number(maxLoginAttempts)
    });
    triggerToast("Administrative entry policies successfully locked down on security cluster.");
  };

  const handleAddAdminSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newAdminName || !newAdminEmail) return;

    onAddAdmin({
      name: newAdminName,
      email: newAdminEmail,
      role: newAdminRole
    });

    setNewAdminName("");
    setNewAdminEmail("");
    setShowAddForm(false);
    triggerToast(`Teammate ${newAdminName} successfully enrolled as ${newAdminRole}.`);
  };

  const handleSelfDestructSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (selfDestructConfirmText.toUpperCase() === "CONFIRM SELF DESTRUCT") {
      setPanicDeploySuccess(true);
      setShowSelfDestructModal(false);
      setSelfDestructConfirmText("");
      setTimeout(() => {
        setPanicDeploySuccess(false);
      }, 6000);
    } else {
      alert("Invalid verification protocol. Emergency cancel.");
    }
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-[1400px] mx-auto animate-fade-in text-white text-left relative">
      
      {/* Dynamic Success Toast */}
      {showSuccessToast && (
        <div className="fixed top-6 right-6 z-50 bg-[#00f59b] text-[#0c0b18] px-5 py-3.5 rounded-xl shadow-[0_8px_30px_rgba(0,245,155,0.3)] border border-[#00f59b] flex items-center gap-3 animate-fade-in">
          <CheckCircle className="w-5 h-5 flex-shrink-0" />
          <span className="font-sans text-xs font-bold">{toastMessage}</span>
        </div>
      )}

      {/* Dynamic Self Destruct Modal */}
      {showSelfDestructModal && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-50 flex items-center justify-center p-4">
          <div className="bg-[#0c0b18] border-2 border-[#fc2e5c] w-full max-w-md rounded-2xl p-6 relative shadow-2xl animate-scale-up">
            <button 
              onClick={() => {
                setShowSelfDestructModal(false);
                setSelfDestructConfirmText("");
              }}
              className="absolute top-4 right-4 text-[#8e8a9f] hover:text-white cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
            
            <div className="flex items-center gap-3 text-[#fc2e5c] mb-4">
              <AlertTriangle className="w-8 h-8 animate-bounce" />
              <h5 className="font-sans font-bold text-base text-white">Emergency Purge Protocol</h5>
            </div>

            <p className="font-sans text-xs text-[#8e8a9f] leading-relaxed mb-5">
              Warning: This launches immediate cryptographic wipe instructions to all registered mobile and tablet endpoints. Standard directory entries will be purged.
            </p>

            <form onSubmit={handleSelfDestructSubmit} className="flex flex-col gap-4">
              <div className="flex flex-col gap-1.5">
                <label className="font-sans text-[10px] text-[#fc2e5c] font-bold uppercase tracking-wider">
                  Type 'CONFIRM SELF DESTRUCT'
                </label>
                <input
                  type="text"
                  required
                  placeholder="Type signature verification text"
                  className="w-full bg-[#131127] border border-[#252243] rounded-xl px-4 py-2.5 outline-none font-sans text-xs focus:border-[#fc2e5c] text-white"
                  value={selfDestructConfirmText}
                  onChange={(e) => setSelfDestructConfirmText(e.target.value)}
                />
              </div>

              <div className="flex gap-3 mt-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowSelfDestructModal(false);
                    setSelfDestructConfirmText("");
                  }}
                  className="flex-1 bg-[#131127] border border-[#252243] text-[#8e8a9f] py-2.5 rounded-xl text-xs hover:text-white transition-all cursor-pointer"
                >
                  Abrupt Abort
                </button>
                <button
                  type="submit"
                  className="flex-1 bg-[#fc2e5c] hover:bg-red-700 text-white font-sans text-xs font-bold py-2.5 rounded-xl transition-all cursor-pointer shadow-[0_4px_12px_rgba(252,46,92,0.3)]"
                >
                  Authorize Purge
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Top Title Banner */}
      <section className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 flex flex-col justify-start items-start text-left shadow-md">
        <h3 className="font-sans font-bold text-base text-white">
          Administrative Security & Directories
        </h3>
        <p className="font-sans text-xs text-[#8e8a9f] mt-1.5 leading-relaxed font-semibold">
          Secure active hardware token validations, configure operator directory overrides, and sync cluster policy mandates.
        </p>
      </section>

      {/* Verification Warning State banner */}
      {panicDeploySuccess && (
        <section className="bg-[#fc2e5c]/10 border border-[#fc2e5c]/30 rounded-2xl p-5 flex items-center gap-4 text-left animate-slide-down">
          <div className="w-10 h-10 bg-[#fc2e5c]/15 border border-[#fc2e5c]/20 rounded-xl flex items-center justify-center text-[#fc2e5c]">
            <ShieldAlert className="w-5 h-5 animate-pulse" />
          </div>
          <div className="flex flex-col gap-0.5">
            <h5 className="font-sans font-bold text-xs text-white">SELF-DESTRUCT SIGNAL STREAMINGED</h5>
            <p className="font-sans text-[11px] text-[#ff8ba4]">Cryptographic wipe vectors disoriented. Disassociated endpoint clusters now hard-bricked.</p>
          </div>
        </section>
      )}

      {/* Grid: Administrators list and credential policies */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        
        {/* Operators team list (Left: 3 cols) */}
        <section className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 lg:col-span-3 shadow-md">
          <div className="flex justify-between items-center mb-5">
            <h4 className="font-sans font-bold text-sm text-white">
              Enrolled Security Operators
            </h4>
            <button
              onClick={() => setShowAddForm(!showAddForm)}
              className="font-sans text-xs text-[#00f59b] hover:text-[#00f59b]/90 transition-all flex items-center gap-1.5 cursor-pointer font-bold select-none"
            >
              {showAddForm ? <X className="w-4 h-4" /> : <UserPlus className="w-4 h-4" />}
              <span>{showAddForm ? "Cancel Operator" : "Enroll Operator"}</span>
            </button>
          </div>

          {/* Collapsible enroll operators form */}
          {showAddForm && (
            <form onSubmit={handleAddAdminSubmit} className="bg-[#131127] border border-[#252243] p-5 rounded-2xl mb-6 flex flex-col gap-4 animate-slide-down">
              <span className="font-sans text-xs text-[#00f59b] font-bold block uppercase tracking-wider">
                Enroll New Admin/Analyst Account
              </span>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="font-sans text-[10px] text-[#8e8a9f] font-bold uppercase tracking-wider">Operator Name</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. David Vance"
                    className="bg-[#0c0b18] border border-[#1e1c31] px-4 py-2.5 rounded-xl font-sans text-xs focus:border-[#6122e6] outline-none text-white"
                    value={newAdminName}
                    onChange={(e) => setNewAdminName(e.target.value)}
                  />
                </div>
                <div className="flex flex-col gap-1.5">
                  <label className="font-sans text-[10px] text-[#8e8a9f] font-bold uppercase tracking-wider">Secure Email</label>
                  <input
                    type="email"
                    required
                    placeholder="e.g. d.vance@guardian.sys"
                    className="bg-[#0c0b18] border border-[#1e1c31] px-4 py-2.5 rounded-xl font-sans text-xs focus:border-[#6122e6] outline-none text-white"
                    value={newAdminEmail}
                    onChange={(e) => setNewAdminEmail(e.target.value)}
                  />
                </div>
              </div>
              <div className="flex flex-col gap-1.5">
                <label className="font-sans text-[10px] text-[#8e8a9f] font-bold uppercase tracking-wider">Clearance Role</label>
                <select
                  className="bg-[#0c0b18] border border-[#1e1c31] px-4 py-2.5 rounded-xl font-sans text-xs focus:border-[#6122e6] outline-none text-white cursor-pointer"
                  value={newAdminRole}
                  onChange={(e) => setNewAdminRole(e.target.value as any)}
                >
                  <option value="Analyst">Analyst (Triage Logs, Notifications Only)</option>
                  <option value="Super Admin">Super Admin (Global System Policies override)</option>
                </select>
              </div>
              <button
                type="submit"
                className="bg-[#6122e6] text-white font-sans text-xs font-bold py-3 rounded-xl hover:bg-[#501bc5] transition-all cursor-pointer mt-2 shadow-[0_4px_12px_rgba(97,34,230,0.3)]"
              >
                Sync Teammate Credentials
              </button>
            </form>
          )}

          {/* Admins Table list */}
          <div className="overflow-x-auto">
            <table className="w-full text-left font-sans text-xs">
              <thead>
                <tr className="border-b border-[#1e1c31] text-[#8e8a9f] font-sans text-[11px] uppercase tracking-wider font-semibold">
                  <th className="py-3 pl-2">Teammate Spec</th>
                  <th className="py-3">Portal Role</th>
                  <th className="py-3 text-right pr-2">Last Terminal Sync</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#1e1c31]/30">
                {admins.map((admin) => (
                  <tr key={admin.id} className="hover:bg-[#131127]/60 transition-colors">
                    <td className="py-4 pl-2 flex items-center gap-2.5">
                      <div className="w-8 h-8 rounded-full bg-[#6122e6]/10 border border-[#6122e6]/20 text-[#00f59b] font-sans text-xs flex items-center justify-center font-bold">
                        {admin.name.split(" ").map(n => n[0]).join("")}
                      </div>
                      <div className="flex flex-col">
                        <span className="font-sans font-bold text-sm text-white">{admin.name}</span>
                        <span className="font-sans text-[10px] text-[#8e8a9f] font-semibold mt-0.5">{admin.email}</span>
                      </div>
                    </td>
                    <td className="py-4">
                      <span className={`px-2.5 py-1 rounded-lg font-sans text-[10px] font-bold ${
                        admin.role === "Super Admin"
                          ? "bg-[#fc2e5c]/10 border border-[#fc2e5c]/25 text-[#ff8ba4]"
                          : "bg-blue-500/10 border border-blue-500/25 text-blue-400"
                      }`}>
                        {admin.role.toUpperCase()}
                      </span>
                    </td>
                    <td className="py-4 text-right pr-2 font-mono text-[11px] text-[#8e8a9f]">
                      {admin.lastLogin}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        {/* Administrative password entry controls (Right: 2 cols) */}
        <section className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 lg:col-span-2 flex flex-col gap-5 shadow-md">
          <h4 className="font-sans font-bold text-sm text-white border-b border-[#1e1c31]/60 pb-3">
            Admin Entry Mandates
          </h4>

          {/* Checkbox 2FA require */}
          <div className="flex flex-col gap-1.5 mt-1">
            <label className="flex items-start gap-3 cursor-pointer group select-none">
              <input
                type="checkbox"
                className="w-4.5 h-4.5 rounded border-[#252243] bg-[#131127] text-[#6122e6] focus:ring-[#6122e6] cursor-pointer"
                checked={require2FA}
                onChange={(e) => setRequire2FA(e.target.checked)}
              />
              <div className="flex flex-col leading-tight -mt-0.5">
                <span className="font-sans text-xs font-bold text-white group-hover:text-[#00f59b] transition-colors">
                  Mandate Strict 2FA Validation
                </span>
                <span className="font-sans text-[10px] text-[#8e8a9f] leading-normal font-semibold mt-1">
                  All Analysts and Super Admins must enroll active hardware tokens. Safe bypass forbidden.
                </span>
              </div>
            </label>
          </div>

          {/* Session timeout */}
          <div className="flex flex-col gap-1.5 mt-3">
            <label className="font-sans text-[10px] font-bold text-[#8e8a9f] uppercase tracking-wider">
              Force Console Session Timeout (mins)
            </label>
            <input
              type="number"
              className="w-full bg-[#131127] border border-[#252243] rounded-xl px-4 py-2.5 outline-none font-sans text-xs focus:border-[#6122e6] text-white"
              value={sessionTimeout}
              onChange={(e) => setSessionTimeout(Number(e.target.value))}
            />
          </div>

          {/* Max login attempt failures */}
          <div className="flex flex-col gap-1.5">
            <label className="font-sans text-[10px] font-bold text-[#8e8a9f] uppercase tracking-wider">
              Enforce Console Max Fails Lockout
            </label>
            <input
              type="number"
              className="w-full bg-[#131127] border border-[#252243] rounded-xl px-4 py-2.5 outline-none font-sans text-xs focus:border-[#6122e6] text-white"
              value={maxLoginAttempts}
              onChange={(e) => setMaxLoginAttempts(Number(e.target.value))}
            />
          </div>

          <button
            onClick={handleSavePolicy}
            className="w-full bg-[#131127] border border-[#252243] text-[#00f59b] font-sans text-xs font-semibold py-3.5 rounded-xl hover:bg-[#6122e6] hover:text-white hover:border-[#6122e6] transition-all cursor-pointer mt-2 flex items-center justify-center gap-1.5"
          >
            <ShieldCheck className="w-4 h-4" />
            <span>Apply Entry Mandates</span>
          </button>
        </section>

      </div>

      {/* Danger Zone Section */}
      <section className="bg-[#fc2e5c]/5 border border-[#fc2e5c]/20 rounded-2xl p-6 flex flex-col md:flex-row items-center justify-between gap-5 relative overflow-hidden text-left shadow-md">
        {/* Soft warning side panel glow */}
        <div className="absolute top-0 left-0 w-1.5 h-full bg-[#fc2e5c]" />

        <div className="flex flex-col text-left gap-1">
          <span className="font-sans text-[10px] text-[#ff8ba4] font-bold uppercase tracking-wider">
            Critical Systems Danger Stripe
          </span>
          <h4 className="font-sans font-bold text-sm text-white">
            Absolute Hardware Panic Mode
          </h4>
          <p className="font-sans text-xs text-[#8e8a9f] leading-relaxed max-w-2xl mt-1 font-semibold">
            Instantly dispatch global remote cryptographic erasure commands to every single active or blocked device listed in directories. All sub-connected systems will destroy standard storage blocks. This is non-reversible.
          </p>
        </div>

        <button
          onClick={() => {
            setShowSelfDestructModal(true);
          }}
          className="bg-[#fc2e5c] text-white hover:bg-red-700 border border-[#fc2e5c] px-5 py-3 rounded-xl font-sans text-xs font-bold transition-all cursor-pointer shrink-0 flex items-center gap-1.5 shadow-[0_4px_12px_rgba(252,46,92,0.3)]"
        >
          <Trash2 className="w-4 h-4" />
          <span>Dispatched Emergency Self-Destruct</span>
        </button>
      </section>

    </div>
  );
}
