BIN_DIR=`pwd`/`dirname $0`
DEPLOY_DIR=$BIN_DIR/..
LIB_DIR=$DEPLOY_DIR/lib
LOGS_DIR=$DEPLOY_DIR/logs
LIB_JARS=`ls $LIB_DIR | grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`
CONF_DIR=$DEPLOY_DIR/conf

PIDS=`ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The client already started!"
    echo "PID: $PIDS"
    exit 1
fi
if [ ! -d $LOGS_DIR ]; then
    mkdir $LOGS_DIR
fi
nohup java -classpath $CONF_DIR:$LIB_JARS com.aioff.spider.videospider.SpiderMain > $LOGS_DIR/stdout.log 2>&1 &
echo "OK!"
PIDS=`ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}'`
echo "PID: $PIDS"
