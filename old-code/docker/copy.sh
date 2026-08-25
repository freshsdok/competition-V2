#!/bin/sh

# 复制项目的文件到对应docker路径，便于一键生成镜像。
usage() {
	echo "Usage: sh copy.sh"
	exit 1
}


# copy sql
echo "begin copy sql "
cp ../sql/ry_20250523.sql ./mysql/db
cp ../sql/ry_config_20250224.sql ./mysql/db

# copy html
echo "begin copy html "
cp -r ../teaching-ui/dist/** ./nginx/html/dist


# copy jar
echo "begin copy teaching-gateway "
cp ../teaching-gateway/target/teaching-gateway.jar ./teaching/gateway/jar

echo "begin copy teaching-auth "
cp ../teaching-auth/target/teaching-auth.jar ./teaching/auth/jar

echo "begin copy teaching-visual "
cp ../teaching-visual/teaching-monitor/target/teaching-visual-monitor.jar  ./teaching/visual/monitor/jar

echo "begin copy teaching-modules-system "
cp ../teaching-modules/teaching-system/target/teaching-modules-system.jar ./teaching/modules/system/jar

echo "begin copy teaching-modules-file "
cp ../teaching-modules/teaching-file/target/teaching-modules-file.jar ./teaching/modules/file/jar

echo "begin copy teaching-modules-job "
cp ../teaching-modules/teaching-job/target/teaching-modules-job.jar ./teaching/modules/job/jar

echo "begin copy teaching-modules-gen "
cp ../teaching-modules/teaching-gen/target/teaching-modules-gen.jar ./teaching/modules/gen/jar

