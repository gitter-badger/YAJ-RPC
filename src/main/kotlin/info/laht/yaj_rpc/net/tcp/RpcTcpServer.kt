/*
 * The MIT License
 *
 * Copyright 2018 Lars Ivar Hatledal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package info.laht.yaj_rpc.net.tcp

import info.laht.yaj_rpc.RpcHandler
import info.laht.yaj_rpc.net.RpcServer

import java.io.IOException
import java.io.BufferedInputStream
import java.io.BufferedOutputStream

import java.net.ServerSocket
import java.net.Socket

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.charset.Charset


open class RpcTcpServer(
        val handler: RpcHandler
): RpcServer {

    private var stop = false;
    private var server: ServerSocket? = null

    override fun start(port: Int) {
        if (server == null) {
            server = ServerSocket(port)
            LOG.info("RpcTcpServer listening for connections on port: $port")
            Thread({
                while(!stop) {
                    try {
                        val accept: Socket = server!!.accept()!!
                        LOG.info("Client with address ${accept.remoteSocketAddress} connected!")
                        val handler = ClientHandler(accept)
                        Thread(handler).start()
                    } catch (ex: IOException) {

                    }
                }
            }).start()
        } else {
            LOG.warn("RpcTcpServer has already been started!")
        }
    }

    override fun stop() {
        stop = true
        server?.close()
        LOG.debug("RpcTcpServer server stopped!")
    }

    inner class ClientHandler(
            socket: Socket
    ): Runnable {

        val `in` = BufferedInputStream(socket.getInputStream())
        val out = BufferedOutputStream(socket.getOutputStream())

        override fun run() {

            try {
                val lenBuf = ByteArray(4)
                while (!stop) {

                    `in`.read(lenBuf, 0, lenBuf.size)
                    val len = ByteBuffer.wrap(lenBuf).int
                    val msg = ByteArray(len).also {
                        `in`.read(it, 0, len)
                    }.let { String(it).replace(0.toChar(), ' ').trim() }

                    if (msg.isNotEmpty()) {
                        handler.handle(msg)?.also {
                            write(it)
                        }
                    }

                }
            } catch (ex: IOException) {
               // LOG.error("Exception caught in TCP client handler thread", ex)
            }
        }

        private fun write(data: String) {
            val bytes = data.toByteArray()
            val len = bytes.size.let { (ByteBuffer.allocate(4).putInt(it).array()) }
            out.write(len)
            out.write(bytes)
            out.flush()
        }

    }

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(RpcTcpServer::class.java)
    }

}