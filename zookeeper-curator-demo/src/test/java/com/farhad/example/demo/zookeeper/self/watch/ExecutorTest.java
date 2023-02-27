package com.farhad.example.demo.zookeeper.self.watch;

/**
 * Run:
 * 
 *    java Executor localhost /stuff output.txt src/main/scripts/count.sh
 * 
 * The arguments are:
 *
 *    - 'localhost' - This tells it that the server is on localhost (at the default 2181 port)
 * 
 *    - '/stuff' - This is the znode it is going to watch
 * 
 *    - 'output.txt' - The name of the text file it will update with the contents of the znode. 
 *      (It just writes to the current directory.)
 * 
 *    - 'src/main/scripts/count.sh' - The program it will start/stop/restart. This is a trivial 
 *       script that simply counts up with a five second delay, and displays the contents of its 
 *       "configuration file".
 * 
 * count.sh: 
 * 
 *   count=0
 *   while [ true ]; do
 *   	let count+=1
 *   	echo Count: $count using `cat output.txt`
 *   	sleep 5
 *   done
 * 
 * Once the Watcher/Executor is running, you'll need to use a client to modify the znode you 
 * told it to 'watch' (/stuff in this case).
 * 
 * In the terminal window with your CLI, execute:
 * 
 *    ] create /stuff my_data
 * 
 * You should see the Watcher/Executor example run your program, './count.sh' in this case, which prints consecutive integers starting with 1.
 * 
 * You should also see the output.txt file appear, with the contents 'my_data' (from above when you created it). Now modify the znode:
 * 
 *    ] set /stuff other_data
 * 
 * You should see the Watcher/Executor example stop and start your program. In this case it will stop counting and then start over at 1.
 * 
 * You should also see the contents of the output.txt file change to 'other_data', the new contents of the znode.
 * 
 * Try setting the znode to another string of text and see what happens.
 * 
 *      ] delete /stuff
 * 
 *  You should see the Watcher/Executor example say "Killing process" and the script should stop counting.
 * 
 *  http://zookeeper.apache.org/doc/trunk/zookeeperStarted.html
 */
public class ExecutorTest {
    
}
