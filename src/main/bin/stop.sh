BIN_DIR=`pwd`/`dirname $0`
DEPLOY_DIR=$BIN_DIR/..
LIB_DIR=$DEPLOY_DIR/lib
LIB_JARS=`ls $LIB_DIR | grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`
SERVER_NAME='dirname $0'

PIDS=`ps x | grep java | grep "$DEPLOY_DIR" | awk '{print $1}'`
if [ -z "$PIDS" ]; then
    echo "ERROR: The client does not started!"
    echo "PID: $PIDS"
    exit 1
fi

echo -e "Stopping the $SERVER_NAME ...\c"
for PID in $PIDS ; do
    kill $PID > /dev/null 2>&1
done


COUNT=0
while [ $COUNT -lt 1 ]; do
    echo -e ".\c"
    sleep 1
    COUNT=1
    for PID in $PIDS ; do
        PID_EXIST=`ps -f -p $PID | grep java`
        if [ -n "$PID_EXIST" ]; then
            COUNT=0
            break
        fi
    done
done

echo "OK!"
echo "PID: $PIDS"
