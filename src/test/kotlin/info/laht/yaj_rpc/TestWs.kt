package info.laht.yaj_rpc

import info.laht.yaj_rpc.net.AbstractAsyncRpcClient
import info.laht.yaj_rpc.net.RpcServer
import info.laht.yaj_rpc.net.ws.RpcWebSocketClient
import info.laht.yaj_rpc.net.ws.RpcWebSocketServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TestWs {

    lateinit var server: RpcServer
    lateinit var client: AbstractAsyncRpcClient

    @Before
    fun setup() {

        val port = ServerSocket(0).use { it.localPort }
        server = RpcWebSocketServer(RpcHandler(SampleService())).also {
            it.start(port)
        }

        client = RpcWebSocketClient("localhost", port)

    }

    @After
    fun tearDown() {
        client.close()
        server.stop()
    }

    @Test
    fun test1() {

        val latch = CountDownLatch(1)
        client.writeAsync("SampleService.greet", RpcParams.listParams("Clint Eastwood"), {
            println("Async response=${it.getResult(String::class.java)}")
            latch.countDown()
        })
        latch.await(1000, TimeUnit.MILLISECONDS)

        client.write("SampleService.greet", RpcListParams("Clint Eastwood")).also {
            println("Synchronous response=${it.getResult(String::class.java)}")
        }

    }

}