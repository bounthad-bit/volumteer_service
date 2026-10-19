export const ROLE_OPTIONS = [
  { value: 1, label: "志愿者" },
  { value: 2, label: "活动组织者" },
  { value: 3, label: "系统管理员" }
];

export const ACTIVITY_STATUS = [
  { value: 0, label: "待审核", type: "warning" },
  { value: 1, label: "招募中", type: "success" },
  { value: 2, label: "进行中", type: "primary" },
  { value: 3, label: "已结束", type: "info" },
  { value: 4, label: "已取消", type: "danger" }
];

export const REGISTRATION_STATUS = [
  { value: 0, label: "待审核", type: "warning" },
  { value: 1, label: "已通过", type: "success" },
  { value: 2, label: "已拒绝", type: "danger" },
  { value: 3, label: "已取消", type: "info" }
];

const find = (list, value) => list.find((i) => i.value === value) || { label: "-", type: "info" };

export const activityStatusLabel = (v) => find(ACTIVITY_STATUS, v).label;
export const activityStatusType = (v) => find(ACTIVITY_STATUS, v).type;
export const registrationStatusLabel = (v) => find(REGISTRATION_STATUS, v).label;
export const registrationStatusType = (v) => find(REGISTRATION_STATUS, v).type;
export const roleLabel = (v) => find(ROLE_OPTIONS, v).label;
