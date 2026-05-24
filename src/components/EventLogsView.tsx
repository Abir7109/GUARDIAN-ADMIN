/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { EventLog, EventLogType, EventLogStatus } from "../types";
import {
  ChevronDown,
  ChevronUp,
  CheckCircle2,
  AlertCircle
} from "lucide-react";

interface EventLogsViewProps {
  logs: EventLog[];
  onUpdateStatus: (id: string, status: EventLogStatus) => void;
  onAddActionTrail: (id: string, text: string) => void;
}

export default function EventLogsView({
  logs,
  onUpdateStatus,
  onAddActionTrail
}: EventLogsViewProps) {
  const [filterType, setFilterType] = useState<EventLogType | 'All'>('All');
  const [expandedLogId, setExpandedLogId] = useState<string | null>("evt_9832749823"); // Default expand first high-stress log
  const [trailInput, setTrailInput] = useState("");

  const filteredLogs = logs.filter((log) => {
    if (filterType === 'All') return true;
    return log.type === filterType;
  });

  const handleToggleRow = (id: string) => {
    setExpandedLogId(expandedLogId === id ? null : id);
  };

  const handleAddTrailItem = (id: string, e: React.FormEvent) => {
    e.preventDefault();
    if (!trailInput.trim()) return;
    onAddActionTrail(id, trailInput.trim());
    setTrailInput("");
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-[1400px] mx-auto animate-fade-in text-white text-left">
      
      {/* Search selection top bar */}
      <section className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl p-6 flex flex-col md:flex-row items-center justify-between gap-4 shadow-md">
        <div className="flex flex-col">
          <h3 className="font-sans font-bold text-base text-white">
            Operational Audit logs & Security Traces
          </h3>
          <span className="font-sans text-xs text-[#8e8a9f] mt-1">
            Collapsible nested encryption payloads, active telemetry streams, and manual triage checklists.
          </span>
        </div>

        {/* Filter categories tabs */}
        <div className="flex bg-[#131127] border border-[#252243] p-1.5 rounded-xl select-none">
          {['All', 'Trigger', 'Alarm', 'Access'].map((type) => {
            const isSelected = filterType === type;
            return (
              <button
                key={type}
                onClick={() => setFilterType(type as any)}
                className={`px-4.5 py-1.5 font-sans text-xs font-semibold rounded-lg cursor-pointer transition-all ${
                  isSelected
                    ? "bg-[#252243] text-[#00f59b]"
                    : "text-[#8e8a9f] hover:text-white"
                }`}
              >
                {type}
              </button>
            );
          })}
        </div>
      </section>

      {/* Primary collapsible table logs */}
      <section className="bg-[#0c0b18] border border-[#1e1c31] rounded-2xl overflow-hidden p-6 shadow-md">
        <h4 className="font-sans font-bold text-sm text-white mb-5">
          Real-Time Audit Records
        </h4>

        <div className="overflow-x-auto">
          <table className="w-full text-left font-sans text-xs">
            <thead>
              <tr className="border-b border-[#1e1c31] text-[#8e8a9f] font-sans text-[11px] uppercase tracking-wider font-semibold">
                <th className="py-3 pl-4">Type</th>
                <th className="py-3">Interface</th>
                <th className="py-3">Status Description</th>
                <th className="py-3">Status State</th>
                <th className="py-3 text-right pr-4">UTC Timestamp</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#1e1c31]/30">
              {filteredLogs.map((log) => {
                const isExpanded = expandedLogId === log.id;
                const isTrigger = log.type === "Trigger";
                const isAlarm = log.type === "Alarm";

                return (
                  <React.Fragment key={log.id}>
                    {/* Primary Row */}
                    <tr
                      onClick={() => handleToggleRow(log.id)}
                      className={`hover:bg-[#131127]/60 transition-colors cursor-pointer group ${
                        isExpanded ? "bg-[#131127]/40" : ""
                      }`}
                    >
                      <td className="py-4 pl-4">
                        <span
                          className={`px-2.5 py-1 rounded-lg font-sans text-[10px] font-semibold ${
                            isTrigger
                              ? "bg-[#fc2e5c]/10 text-[#ff8ba4] border border-[#fc2e5c]/20"
                              : isAlarm
                              ? "bg-amber-400/10 text-amber-300 border border-amber-400/20"
                              : "bg-[#00f59b]/10 text-[#00f59b] border border-[#00f59b]/20"
                          }`}
                        >
                          {log.type}
                        </span>
                      </td>
                      <td className="py-4 font-mono text-white text-xs">
                        {log.source}
                      </td>
                      <td className="py-4 text-[#8e8a9f] font-medium max-w-[280px] truncate">
                        {log.details}
                      </td>
                      <td className="py-4">
                        <span className={`inline-flex items-center gap-1.5 font-sans text-xs font-semibold ${
                          log.status === "Investigating"
                            ? "text-amber-300 animate-pulse"
                            : log.status === "Resolved"
                            ? "text-[#00f59b]"
                            : "text-[#8e8a9f]/60"
                        }`}>
                          <span className={`w-1.5 h-1.5 rounded-full ${
                            log.status === "Investigating"
                              ? "bg-amber-300"
                              : log.status === "Resolved"
                              ? "bg-[#00f59b]"
                              : "bg-gray-500"
                          }`} />
                          {log.status}
                        </span>
                      </td>
                      <td className="py-4 text-right pr-4 font-mono text-[#8e8a9f] select-none">
                        <div className="flex items-center justify-end gap-2.5">
                          <span>{log.timestamp}</span>
                          {isExpanded ? <ChevronUp className="w-4 h-4 text-[#8e8a9f]/50" /> : <ChevronDown className="w-4 h-4 text-[#8e8a9f]/50" />}
                        </div>
                      </td>
                    </tr>

                    {/* Collapsible Expanded Payloads Nested Block */}
                    {isExpanded && (
                      <tr className="bg-[#131127]/50">
                        <td colSpan={5} className="p-6 border-b border-[#1e1c31]/50">
                          <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
                            
                            {/* Left: Raw formatted JSON payload details */}
                            <div className="lg:col-span-3 flex flex-col gap-2 text-left">
                              <span className="font-sans text-[11px] text-[#00f59b] font-bold uppercase tracking-wider block">
                                Raw Encryption & Envelope Metadata (JSON)
                              </span>
                              <pre className="p-4 bg-[#131127] border border-[#252243] rounded-xl text-[11px] font-mono leading-normal text-[#a5f3fc]/90 overflow-x-auto max-h-72 shadow-inner">
                                {JSON.stringify(log.payload, null, 2)}
                              </pre>
                            </div>

                            {/* Right: Active administrative checklist & triage tools */}
                            <div className="lg:col-span-2 flex flex-col gap-4 text-left">
                              <span className="font-sans text-[11px] text-[#fc2e5c] font-bold uppercase tracking-wider block">
                                Administrative Triage checklist
                              </span>

                              {/* Interactive check steps timeline checklist */}
                              <div className="flex flex-col gap-3.5 bg-[#131127] border border-[#252243] rounded-xl p-4 shadow-inner">
                                {log.actionTrail.map((trial, index) => (
                                  <div key={index} className="flex gap-2.5 items-start justify-start text-[11.5px]">
                                    <CheckCircle2 className="w-4.5 h-4.5 text-[#00f59b] shrink-0 mt-0.5" />
                                    <div className="flex flex-col leading-snug">
                                      <p className="font-sans text-white font-semibold">{trial.text}</p>
                                      <span className="font-sans text-[10px] text-[#8e8a9f] mt-0.5">{trial.timestamp}</span>
                                    </div>
                                  </div>
                                ))}

                                {/* Compose/Add custom custom actions step form */}
                                <form onSubmit={(e) => handleAddTrailItem(log.id, e)} className="flex gap-2 mt-2">
                                  <input
                                    type="text"
                                    className="flex-1 bg-[#0c0b18] border border-[#1e1c31] rounded-xl px-3.5 py-2 outline-none font-sans text-xs focus:border-[#6122e6] text-white"
                                    placeholder="Add manual triage action..."
                                    value={trailInput}
                                    onChange={(e) => setTrailInput(e.target.value)}
                                  />
                                  <button
                                    type="submit"
                                    className="bg-[#6122e6] hover:bg-[#501bc5] text-white px-4 py-2 rounded-xl font-sans text-xs font-semibold cursor-pointer transition-all shrink-0"
                                  >
                                    Add Log
                                  </button>
                                </form>
                              </div>

                              {/* Triage Resolve buttons */}
                              <div className="flex gap-2 mt-1">
                                <button
                                  onClick={() => onUpdateStatus(log.id, "Resolved")}
                                  disabled={log.status === "Resolved"}
                                  className="flex-1 font-sans font-bold text-xs bg-[#00f59b]/10 border border-[#00f59b]/25 hover:bg-[#00f59b]/20 text-[#00f59b] py-2.5 rounded-xl text-center cursor-pointer transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                                >
                                  Mark Threat Resolved
                                </button>
                                <button
                                  onClick={() => onUpdateStatus(log.id, "Investigating")}
                                  disabled={log.status === "Investigating"}
                                  className="flex-1 font-sans font-bold text-xs bg-amber-400/10 border border-amber-400/25 hover:bg-amber-400/25 text-amber-300 py-2.5 rounded-xl text-center cursor-pointer transition-colors disabled:opacity-40"
                                >
                                  Escalate Triage
                                </button>
                              </div>

                            </div>

                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>

    </div>
  );
}
