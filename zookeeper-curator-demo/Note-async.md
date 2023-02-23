### Curator Async
#
# With this DSL you can do asynchronous tasks in a more natural, functional way using Java 8 lambdas.
#
#   // let "client" be a CuratorFramework instance
#   AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);
#   async.checkExists().forPath(somePath).thenAccept(stat -> mySuccessOperation(stat));
#
## Usage
#
# Note: To use Curator Async, you should be familiar with Java 8's lambdas, CompletedFuture and CompletionStage.
#
# Create a CuratorFramework instance in the normal way. You then wrap this instance using AsyncCuratorFramework. 
# 
#   // let "client" be a CuratorFramework instance
#   AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);
#
# AsyncCuratorFramework has most of the same builder methods that CuratorFramework does with some important differences:
#
#  - AsyncCuratorFramework builders return AsyncStage instances
#  - AsyncCuratorFramework builders have no checked exceptions
#  - Many of the builder methods have been simplified/clarified
#  - All builders invoke the asynchronous versions of ZooKeeper APIs
#  - Watchers also use CompletionStages - see below for details
#
## AsyncStage
#
# AsyncStage instances extend Java 8's CompletionStage. CompletionStage objects can be "completed" with a success value 
# or an exception. 
#
# The parameterized type of the AsyncStage will be whatever the builder used would naturally return as a success value. 
# E.g. the async getData() builder's AsyncStage is parameterized with "byte[]".
#
## Watchers
#
# ZooKeeper watchers also get the CompletionStage treatment in Curator Async. To add a watcher, call watched() prior to 
# starting the appropriate builders.
#
#   async.watched().getData().forPath(path) ...
#
# Thus, a data watcher will be set on the specified path. You access the CompletionStage for the watcher by using the 
# event() method of AsyncStage.
#
#   async.watched().getData().forPath(path).event().thenAccept(watchedEvent -> watchWasTriggered(watchedEvent));
#
# ZooKeeper calls watchers when there is a connection loss. 
#
# This can make using the CompletionStage somewhat complicated (see AsyncEventException below). 
#
# If you are not interested in watcher connection problems, you can tell Curator Async to not send them by calling:
#
#   // only complete the CompletionStage when the watcher is successfully triggered
#   // i.e. don't complete on connection issues
#   async.with(WatchMode.successOnly).watched()...
#
## AsyncEventException
#
# When an async watcher fails the exception set in the CompletionStage will be of type AsyncEventException. 
#
# This exception allows you to see the KeeperState that caused the trigger and allows you to reset the completion stage. 
#
# Reset is needed because ZooKeeper temporarily triggers watchers when there is a connection event (unless WatchMode.successOnly 
# is used). However, the watcher stays set for the original operation. 
#
# Use AsyncEventException#reset to start a new completion stage that will wait on the next trigger of the watcher.
#
#   AsyncStage stage = ...
#   stage.event().exceptionally(e -> {
#       AsyncEventException asyncEx = (AsyncEventException)e;
#   
#       ... note a connection problem ...
#   
#       asyncEx.reset().thenAccept(watchedEvent -> watchWasTriggered(watchedEvent));
#   });
#
## AsyncResult
#
# As a convenience, you can use AsyncResult to combine ZooKeeper method value, the ZooKeeper result code and any exception in one 
# object allowing you to not worry about exceptional completions. 
#
# i.e. the CompletionStage returned by AsyncResult.of() always completes successfully with an AsyncResult object.
#
# AsyncResult has methods to get either the method result (a path, Stat, etc.), a KeeperException code or a general exception:
#
#   Optional<T> getValue();
#   
#   KeeperException.Code getCode();
#   
#   Optional<Throwable> getException();
#
# Use AsyncResult by wrapping an AsyncStage value. i.e.
#
#   CompletionStage<AsyncResult<Stat>> resultStage = AsyncResult.of(async.checkExists().forPath(path));
#   resultStage.thenAccept(result -> {
#       if ( result.getValue().isPresent() ) {
#           // ...
#       } else if ( result.getCode() == KeeperException.Code.NOAUTH ) {
#           // ...
#       }
#       // etc.
#   });
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#

