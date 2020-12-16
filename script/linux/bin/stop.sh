SCRIPT_PATH="${BASH_SOURCE[0]}"
SCRIPT_DIR=`dirname ${SCRIPT_PATH}`
BASE_DIR=`cd ${SCRIPT_DIR}/.. && pwd`
PROJ_DIR=`cd ${BASE_DIR}/.. && pwd`
DATA_DIR="${PROJ_DIR}/data"
PROJ_NAME=`basename ${BASE_DIR}`

PID=$(ps -ef | grep ${PROJ_NAME}.jar | grep -v grep | awk '{ print $2 }')
if [ -z "$PID" ]
then
    echo Application is already stopped
else
    echo kill $PID
    kill $PID
    echo Application is stopped
fi