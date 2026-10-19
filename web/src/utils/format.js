export function formatDateTime(value) {
  if (!value) return "-";
  return String(value).replace("T", " ").slice(0, 19);
}

export function formatDuration(minutes) {
  if (minutes === null || minutes === undefined) return "-";
  const total = Number(minutes);
  const h = Math.floor(total / 60);
  const m = total % 60;
  if (h === 0) return `${m} 分钟`;
  return m === 0 ? `${h} 小时` : `${h} 小时 ${m} 分钟`;
}

export function emptyText(value) {
  return value === null || value === undefined || value === "" ? "-" : value;
}
