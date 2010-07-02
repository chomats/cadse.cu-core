rm -f /tmp/test_source_java.txt
cat /home/chomats/licence_apache.txt > /tmp/test_source_java.txt
cat $1 >> /tmp/test_source_java.txt
mv -f  /tmp/test_source_java.txt $1
