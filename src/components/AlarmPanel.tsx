import { SecurityUser } from "../types";
import { X, VolumeX, MapPin, ExternalLink, Users, Copy } from "lucide-react";

interface AlarmPanelProps {
  user: SecurityUser;
  onStopAlarm: (id: string) => void;
  onNavigateToUsers: () => void;
  onClose: () => void;
}

export default function AlarmPanel({ user, onStopAlarm, onNavigateToUsers, onClose }: AlarmPanelProps) {
  const hasLocation = user.lastLatitude && user.lastLongitude;

  const osmUrl = hasLocation
    ? `https://www.openstreetmap.org/export/embed.html?bbox=${user.lastLongitude! - 0.02},${user.lastLatitude! - 0.02},${user.lastLongitude! + 0.02},${user.lastLatitude! + 0.02}&layer=mapnik&marker=${user.lastLatitude},${user.lastLongitude}`
    : "";

  const googleMapsUrl = hasLocation
    ? `https://maps.google.com/?q=${user.lastLatitude},${user.lastLongitude}`
    : "#";

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-[#050f1b]/90 backdrop-blur-md pointer-events-auto p-4 select-none animate-fade-in">
      <div className="bg-[#0c0b18] border border-[#93000a]/60 rounded-2xl w-full max-w-5xl h-[90vh] flex flex-col shadow-2xl shadow-[#93000a]/10 overflow-hidden">

        {/* Header — user info */}
        <div className="shrink-0 flex items-center justify-between px-6 py-4 border-b border-[#93000a]/40 bg-[#93000a]/10">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-[#00ff88]/15 border-2 border-[#ff5e62] flex items-center justify-center text-[#ffea2a] font-mono text-base font-bold uppercase">
              {user.initials}
            </div>
            <div>
              <h2 className="font-sans font-bold text-lg text-white">{user.name}</h2>
              <p className="font-mono text-xs text-[#b9cbb9]/60">{user.email}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="flex items-center gap-1.5 px-3 py-1.5 bg-[#93000a]/20 border border-[#ff5e62]/40 rounded-lg">
              <span className="w-2 h-2 rounded-full bg-[#ff5e62] animate-pulse" />
              <span className="font-mono text-[10px] text-[#ff5e62] font-bold uppercase tracking-wider">Alarm Active</span>
            </span>
            <button
              onClick={onClose}
              className="text-[#b9cbb9]/55 hover:text-white border border-[#2a3441] hover:bg-[#2a3441] w-9 h-9 rounded-lg cursor-pointer flex justify-center items-center transition-all"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="flex-1 flex flex-col lg:flex-row gap-4 p-6 overflow-y-auto min-h-0">

          {/* Left — map */}
          <div className="flex-1 flex flex-col min-h-0">
            <div className="flex items-center justify-between mb-3">
              <h3 className="font-mono text-xs text-[#b9cbb9]/60 uppercase tracking-wider font-semibold">
                Live Location Tracking
              </h3>
              {hasLocation && (
                <a
                  href={googleMapsUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-1.5 text-[#00f59b] hover:text-white font-mono text-[11px] font-semibold transition-colors"
                >
                  <ExternalLink className="w-3.5 h-3.5" />
                  Open in Google Maps
                </a>
              )}
            </div>
            <div className="flex-1 bg-[#06050e] border border-[#2a3441] rounded-xl overflow-hidden relative min-h-[250px]">
              {hasLocation ? (
                <iframe
                  title="OSM Map"
                  src={osmUrl}
                  className="w-full h-full border-0"
                  loading="lazy"
                  referrerPolicy="no-referrer"
                />
              ) : (
                <div className="absolute inset-0 flex flex-col items-center justify-center text-[#b9cbb9]/40 gap-3">
                  <MapPin className="w-10 h-10" />
                  <p className="font-mono text-sm">No location data available</p>
                  <p className="font-mono text-[10px]">Waiting for device GPS fix...</p>
                </div>
              )}
            </div>

            {/* coordinate bar below map */}
            {hasLocation && (
              <div className="mt-2 flex items-center gap-2 bg-[#06050e] border border-[#2a3441] rounded-lg px-4 py-2">
                <MapPin className="w-4 h-4 text-[#00f59b] shrink-0" />
                <span className="font-mono text-sm text-[#d9e3f5] font-semibold">
                  {Number(user.lastLatitude).toFixed(6)}, {Number(user.lastLongitude).toFixed(6)}
                </span>
                <button
                  onClick={() => navigator.clipboard.writeText(`${user.lastLatitude},${user.lastLongitude}`)}
                  className="text-[#b9cbb9]/30 hover:text-white transition-colors ml-auto"
                  title="Copy coordinates"
                >
                  <Copy className="w-4 h-4" />
                </button>
              </div>
            )}
          </div>

          {/* Right — info panel */}
          <div className="w-full lg:w-80 shrink-0 flex flex-col gap-3">
            <h3 className="font-mono text-xs text-[#b9cbb9]/60 uppercase tracking-wider font-semibold">
              Device Intelligence
            </h3>

            <div className="bg-[#06050e] border border-[#2a3441] rounded-xl p-4 space-y-3">
              <InfoRow label="Device" value={user.deviceModel} mono />
              <InfoRow label="OS" value={user.osVersion} mono />
              <InfoRow label="Device ID" value={user.deviceId} mono />
              <InfoRow label="Status" value={user.status} />
              <InfoRow label="Last Active" value={user.lastActive} mono />
              <InfoRow label="Protection" value={user.protectionActive ? "ON" : "OFF"} />
              <InfoRow label="Premium" value={user.isPremium ? "Yes" : "No"} />
            </div>

            {/* quick action cards */}
            <div className="bg-[#06050e] border border-[#2a3441] rounded-xl p-4 space-y-3">
              <h4 className="font-mono text-[10px] text-[#b9cbb9]/40 uppercase tracking-wider">Quick Actions</h4>
              <div className="flex flex-col gap-2">
                <button
                  onClick={() => onNavigateToUsers()}
                  className="flex items-center gap-2 bg-[#2a3441] hover:bg-[#354354] border border-[#3b4b3d]/40 text-[#d9e3f5] py-2.5 px-4 rounded-lg text-xs font-semibold cursor-pointer transition-colors w-full"
                >
                  <Users className="w-4 h-4" />
                  View in Users Panel
                </button>
                {hasLocation && (
                  <a
                    href={googleMapsUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-2 bg-[#2a3441] hover:bg-[#354354] border border-[#3b4b3d]/40 text-[#d9e3f5] py-2.5 px-4 rounded-lg text-xs font-semibold cursor-pointer transition-colors w-full"
                  >
                    <MapPin className="w-4 h-4" />
                    Open Coordinates in Maps
                  </a>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Footer — controls */}
        <div className="shrink-0 flex items-center justify-between px-6 py-4 border-t border-[#93000a]/40 bg-[#93000a]/5">
          <p className="font-mono text-[10px] text-[#b9cbb9]/40">
            Device is broadcasting alarm signal
          </p>
          <div className="flex items-center gap-3">
            <button
              onClick={onClose}
              className="bg-[#2a3441] hover:bg-[#354354] border border-[#3b4b3d]/40 text-[#d9e3f5] py-2.5 px-5 rounded-lg text-xs font-semibold cursor-pointer transition-colors"
            >
              Dismiss
            </button>
            <button
              onClick={() => {
                onStopAlarm(user.id);
                onClose();
              }}
              className="flex items-center gap-2 bg-[#93000a] hover:bg-[#b00014] border border-[#ff5e62]/50 text-white py-2.5 px-5 rounded-lg text-xs font-bold cursor-pointer transition-colors"
            >
              <VolumeX className="w-4 h-4" />
              Stop Alarm
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}

function InfoRow({ label, value, mono }: { label: string; value: string | number | boolean; mono?: boolean }) {
  return (
    <div className="flex items-center justify-between">
      <span className="font-mono text-[10px] text-[#b9cbb9]/40 uppercase tracking-wider">{label}</span>
      <span className={`${mono ? "font-mono" : "font-sans"} text-xs text-[#d9e3f5] font-semibold`}>
        {String(value)}
      </span>
    </div>
  );
}
