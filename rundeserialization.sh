for i in `ls $1`; 
do 
    echo "running with $i"
    curl localhost:8082/checkAccountSimple -X POST --data-urlencode "lol=`cat $i`";
    sleep 2
done
