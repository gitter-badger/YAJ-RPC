package info.laht.yaj_rpc

import info.laht.yaj_rpc.net.AbstractAsyncRpcClient
import info.laht.yaj_rpc.net.RpcServer
import info.laht.yaj_rpc.net.ws.RpcWebSocketClient
import info.laht.yaj_rpc.net.ws.RpcWebSocketServer
import org.junit.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TestWs {

   companion object {

       val LOG: Logger = LoggerFactory.getLogger(TestWs::class.java)

       lateinit var server: RpcServer
       lateinit var client: AbstractAsyncRpcClient

       @JvmStatic
       @BeforeClass
       fun setup() {

           server = RpcWebSocketServer(RpcHandler(SampleService()))
           val port = server.start()

           client = RpcWebSocketClient("localhost", port)

       }

       @JvmStatic
       @AfterClass
       fun tearDown() {
           client.close()
           server.stop()
       }
   }

    @Test
    fun test1() {

        val latch = CountDownLatch(1)
        client.writeAsync("SampleService.greet", RpcParams.listParams("Clint Eastwood"), {
            LOG.info("Async response=${it.getResult(String::class.java)}")
            latch.countDown()
        })
        latch.await(1000, TimeUnit.MILLISECONDS)

        client.write("SampleService.greet", RpcListParams("Clint Eastwood")).also {
            LOG.info("Synchronous response=${it.getResult(String::class.java)}")
        }

    }


}