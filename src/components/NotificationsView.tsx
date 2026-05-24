/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { BroadcastNotification } from "../types";
import {
  Send,
  Smartphone,
  Wifi,
  Battery,
  Shield,
  ArrowRight,
  ShieldAlert,
  Lock,
  Compass
} from "lucide-react";

interface NotificationsViewProps {
  broadcasts: BroadcastNotification[];
  onAddBroadcast: (broadcast: { title: string; body: string; targetAudience: string; actionUrl: string; scheduleForLater: boolean; schedule: string }) => void;
}

export default function NotificationsView({
  broadcasts,
  onAddBroadcast
}: NotificationsViewProps) {
  const [title, setTitle] = useState("SecOS Critical Certificate Revocation");
  const [body, setBody] = useState("A malicious trust anchor has been observed. All personnel must initiate mandatory root CA updates via active VPN tunnels.");
  const [targetAudience, setTargetAudience] = useState("All Active Devices");
  const [actionUrl, setActionUrl] = useState("secure://root/ca_update");
  const [scheduleForLater, setScheduleForLater] = useState(false);
  const [scheduleTime, setScheduleTime] = useState("2026-05-25 09:00 UTC");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title || !body) return;

    onAddBroadcast({
      title,
      body,
      targetAudience,
      actionUrl,
      scheduleForLater,
      schedule: scheduleTime
    });

    setTitle("");
    setBody("");
    setActionUrl("");
    setScheduleForLater(false);
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-[1400px] mx-auto animate-fade-in text-white">
      
      {/* Split Column: Compose & Phone Preview side-by-side */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6 text-left">
        
        {/* Compose Form - 3 cols */}
        <section className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 lg:col-span-3 shadow-md">
          <h3 className="font-sans font-bold text-base text-white mb-4">
            Stitch Broadcast Dispatcher
          </h3>
          <p className="font-sans text-xs text-[#8e8a9f] mb-6 -mt-2 leading-relaxed">
            Broadcast zero-day security advisories, lock requests, and urgent notifications directly into remote terminal hardware frames.
          </p>

          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            {/* Title */}
            <div className="flex flex-col gap-1.5">
              <label className="font-sans text-[11px] font-bold text-[#8e8a9f] uppercase tracking-wider">
                Event Title / Banner
              </label>
              <input
                type="text"
                className="w-full bg-[#131127] border border-[#252243] rounded-xl px-4 py-2.5 outline-none font-sans text-sm focus:border-[#6122e6] text-white"
                placeholder="e.g., Mandatory Lockdown Protocol"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>

            {/* Target Audience SELECTOR */}
            <div className="flex flex-col gap-1.5">
              <label className="font-sans text-[11px] font-bold text-[#8e8a9f] uppercase tracking-wider">
                Audience Security Range
              </label>
              <select
                className="w-full bg-[#131127] border border-[#252243] rounded-xl px-4 py-2.5 outline-none font-sans text-sm focus:border-[#6122e6] text-white cursor-pointer"
                value={targetAudience}
                onChange={(e) => setTargetAudience(e.target.value)}
              >
                <option value="All Active Devices">All Enrolled Devices (Global)</option>
                <option value="Security Clearance A">Security Clearance A (Level 3+ Only)</option>
                <option value="Premium Tier Only">Premium Executives Slots</option>
                <option value="Legacy OS Users">Legacy Operating System Slots</option>
              </select>
            </div>

            {/* Body Description */}
            <div className="flex flex-col gap-1.5">
              <label className="font-sans text-[11px] font-bold text-[#8e8a9f] uppercase tracking-wider">
                Advisory Message Markdown
              </label>
              <textarea
                rows={4}
                className="w-full bg-[#131127] border border-[#252243] rounded-xl px-4 py-2.5 outline-none font-sans text-sm focus:border-[#6122e6] text-white placeholder:text-[#8e8a9f]/30"
                placeholder="Enter core notification synopsis details..."
                value={body}
                onChange={(e) => setBody(e.target.value)}
                required
              />
            </div>

            {/* Action Hook Routing URL (Optional) */}
            <div className="flex flex-col gap-1.5">
              <label className="font-sans text-[11px] font-bold text-[#8e8a9f] uppercase tracking-wider">
                Action Hook Routing URL (Optional)
              </label>
              <input
                type="text"
                className="w-full bg-[#131127] border border-[#252243] rounded-xl px-4 py-2.5 outline-none font-sans text-sm focus:border-[#6122e6] text-white"
                placeholder="secure://action/lock_down_device"
                value={actionUrl}
                onChange={(e) => setActionUrl(e.target.value)}
              />
            </div>

            {/* Scheduler Checkbox Option */}
            <div className="p-4 bg-[#131127] rounded-xl border border-[#252243]">
              <label className="flex items-center gap-3 cursor-pointer select-none">
                <input
                  type="checkbox"
                  className="w-4 h-4 rounded border-[#252243] bg-[#0c0b18] text-[#6122e6] focus:ring-[#6122e6] cursor-pointer"
                  checked={scheduleForLater}
                  onChange={(e) => setScheduleForLater(e.target.checked)}
                />
                <span className="font-sans text-xs font-semibold text-white">
                  Schedule dispatch window for later time slot
                </span>
              </label>

              {scheduleForLater && (
                <div className="mt-4 animate-slide-down flex flex-col gap-1.5">
                  <label className="font-sans text-[10px] text-[#00f59b] font-bold uppercase tracking-wider">
                    Execution Window
                  </label>
                  <input
                    type="text"
                    className="w-full bg-[#0c0b18] border border-[#1e1c31] rounded-xl px-4 py-2 outline-none font-sans text-xs text-[#00f59b]"
                    value={scheduleTime}
                    onChange={(e) => setScheduleTime(e.target.value)}
                  />
                </div>
              )}
            </div>

            {/* Submit Action */}
            <button
              type="submit"
              className="w-full bg-[#6122e6] text-white font-sans text-xs font-semibold py-3.5 rounded-xl hover:bg-[#501bc5] transition-all cursor-pointer shadow-[0_4px_12px_rgba(97,34,230,0.3)] flex items-center justify-center gap-2"
            >
              <Send className="w-4 h-4" />
              <span>{scheduleForLater ? "Schedule Protocol Broadcast" : "Publish Broadcast Immediately"}</span>
            </button>
          </form>
        </section>

        {/* Live Device Outline Mockup - 2 cols */}
        <section className="lg:col-span-2 flex flex-col items-center justify-center">
          <div className="w-full max-w-[290px] relative">
            <span className="font-sans text-[10px] text-[#8e8a9f] font-semibold uppercase tracking-widest block text-center mb-4">
              Stitch Lockscreen Live View
            </span>
            
            {/* Phone enclosure styled strictly to guidelines */}
            <div className="bg-[#0c0b18] border-[8px] border-[#252243] rounded-[40px] min-h-[500px] w-full shadow-2xl relative overflow-hidden flex flex-col p-4">
              
              {/* Phone speaker notch */}
              <div className="absolute top-1.5 left-1/2 -translate-x-1/2 w-20 h-4 bg-[#252243] rounded-full flex items-center justify-center">
                <span className="w-6 h-1 bg-[#1e1a3a] rounded-full" />
              </div>

              {/* Status bar */}
              <div className="flex justify-between items-center text-[10px] font-sans text-[#8e8a9f] mt-1.5 mb-4 px-2 select-none">
                <span>09:41 AM</span>
                <div className="flex items-center gap-1">
                  <Compass className="w-3.5 h-3.5 text-[#8e8a9f]" />
                  <Wifi className="w-3.5 h-3.5 text-[#8e8a9f]" />
                  <Battery className="w-3.5 h-3.5 text-[#8e8a9f]" />
                </div>
              </div>

              {/* Decrypted Lock area */}
              <div className="flex flex-col items-center gap-1.5 mt-2">
                <Lock className="text-[#00f59b] w-7 h-7 animate-pulse" />
                <span className="font-sans font-bold text-2xl text-white">09:41</span>
                <span className="font-sans text-[10px] text-[#8e8a9f]">Tuesday, May 24</span>
              </div>

              {/* LIVE SYNCED BROADCAST ADVISORY CARD */}
              <div className="mt-8 bg-[#131127] border border-[#252243] rounded-2xl p-4 shadow-lg relative flex flex-col text-left">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-5 h-5 bg-[#00f59b]/15 border border-[#00f59b]/20 rounded flex items-center justify-center text-[#00f59b]">
                    <Shield className="w-3.5 h-3.5" />
                  </div>
                  <div className="flex justify-between items-center w-full">
                    <span className="font-sans font-bold text-[10.5px] text-[#00f59b] tracking-wide">
                      SYSTEM ALERT
                    </span>
                    <span className="font-sans text-[9px] text-[#8e8a9f]">
                      Just Now
                    </span>
                  </div>
                </div>

                {/* Core Live Title */}
                <h5 className="font-sans font-bold text-[12px] text-white leading-snug truncate">
                  {title || "Unscheduled System Warning"}
                </h5>

                {/* Core Live Body */}
                <p className="font-sans text-[11px] text-[#8e8a9f] leading-snug mt-1.5 line-clamp-4">
                  {body || "Synchronizing connection keys. Waiting on administrator parameters..."}
                </p>

                {/* Optional CTA Link */}
                {actionUrl && (
                  <div className="mt-4 pt-2.5 border-t border-[#252243] flex items-center justify-between text-[#00f59b]">
                    <span className="font-sans text-[9.5px] font-bold tracking-wider">DISPATCH RESPONSE</span>
                    <ArrowRight className="w-3 h-3" />
                  </div>
                )}
              </div>

              {/* Soft bottom instructions overlay */}
              <div className="mt-auto text-center py-2">
                <span className="font-sans text-[9px] text-[#8e8a9f]/40">
                  Secured by Stitch Secure v4.2
                </span>
              </div>

            </div>
          </div>
        </section>

      </div>

      {/* Broadcast history list */}
      <section className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 text-left shadow-md">
        <h3 className="font-sans font-bold text-sm text-white mb-5">
          Historical Transmission Logs
        </h3>

        <div className="overflow-x-auto">
          <table className="w-full text-left font-sans text-xs">
            <thead>
              <tr className="border-b border-[#1e1c31] text-[#8e8a9f] font-sans text-[11px] uppercase tracking-wider font-semibold">
                <th className="py-3 pl-4">Target Bandwidth</th>
                <th className="py-3">Synopsis Description</th>
                <th className="py-3">Channel State</th>
                <th className="py-3">Dispersion Rate</th>
                <th className="py-3 text-right pr-4">Executed</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#1e1c31]/30">
              {broadcasts.map((bc) => {
                const isSent = bc.status === "Sent";
                const isScheduled = bc.status === "Scheduled";
                const isFailed = bc.status === "Failed";

                return (
                  <tr key={bc.id} className="hover:bg-[#131127]/60 transition-colors">
                    <td className="py-4 pl-4 font-sans font-semibold text-white">
                      {bc.targetAudience}
                    </td>
                    <td className="py-4">
                      <div className="flex flex-col max-w-[360px]">
                        <span className="font-sans font-bold text-xs text-white">{bc.title}</span>
                        <p className="font-sans text-[11px] text-[#8e8a9f] leading-normal line-clamp-1 mt-0.5">{bc.body}</p>
                      </div>
                    </td>
                    <td className="py-4">
                      {isSent ? (
                        <span className="px-2.5 py-1 bg-[#00f59b]/10 border border-[#00f59b]/20 text-[#00f59b] font-sans text-[10px] rounded-lg font-semibold uppercase">
                          Sent
                        </span>
                      ) : isScheduled ? (
                        <span className="px-2.5 py-1 bg-blue-500/10 border border-blue-500/20 text-blue-400 font-sans text-[10px] rounded-lg font-semibold uppercase">
                          Scheduled
                        </span>
                      ) : isFailed ? (
                        <span className="px-2.5 py-1 bg-[#fc2e5c]/10 border border-[#fc2e5c]/20 text-[#ff8ba4] font-sans text-[10px] rounded-lg font-semibold uppercase">
                          Failed
                        </span>
                      ) : (
                        <span className="px-2.5 py-1 bg-gray-500/10 border border-gray-600/20 text-gray-400 font-sans text-[10px] rounded-lg font-semibold uppercase">
                          Draft
                        </span>
                      )}
                    </td>
                    <td className="py-4">
                      {isSent && bc.deliveryRate ? (
                        <div className="flex flex-col gap-1.5 w-24">
                          <span className="font-sans text-[10px] text-[#00f59b] font-semibold">{bc.deliveryRate}% Delivery</span>
                          <div className="w-full h-1 bg-[#131127] rounded-full overflow-hidden border border-[#252243]/40">
                            <div className="h-full bg-[#00f59b]" style={{ width: `${bc.deliveryRate}%` }} />
                          </div>
                        </div>
                      ) : isFailed && bc.errorMessage ? (
                        <span className="font-sans text-[10px] text-[#fc2e5c] font-semibold italic">
                          {bc.errorMessage}
                        </span>
                      ) : (
                        <span className="font-sans text-[#8e8a9f]/40 italic">N/A</span>
                      )}
                    </td>
                    <td className="py-4 text-right pr-4 font-mono text-[11px] text-[#8e8a9f]">
                      {bc.timestamp}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>

    </div>
  );
}
