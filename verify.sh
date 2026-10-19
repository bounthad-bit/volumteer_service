#!/usr/bin/env bash
# 一键检查环境是否满足部署条件（可选，不跑也能部署）
echo "=========== 环境自检 ==========="
printf "%-22s" "JDK:";   java -version 2>&1 | head -1 || echo "未安装（需要 JDK 17+）"
printf "%-22s" "Node.js:"; node -v 2>/dev/null || echo "未安装（需要 18+）"
printf "%-22s" "npm:";    npm -v 2>/dev/null || echo "未安装"
printf "%-22s" "Maven:";  mvn -v 2>/dev/null | head -1 || echo "未安装（也可直接用 target 里的 jar）"
printf "%-22s" "MySQL:";  mysql --version 2>/dev/null || echo "未安装"
echo "--------------------------------"
if mysql -uroot -p"$1" -e "use volunteer_service; select count(*) from activity;" 2>/dev/null; then
  echo "数据库 volunteer_service：已存在，可直接启动后端"
else
  echo "数据库 volunteer_service：未就绪，请先执行"
  echo "    mysql -uroot -p < sql/volunteer_service.sql"
fi
echo "================================"
