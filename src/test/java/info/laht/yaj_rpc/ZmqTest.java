package info.laht.yaj_rpc;

import info.laht.yaj_rpc.net.AbstractRpcClient;
import info.laht.yaj_rpc.net.RpcServer;
import info.laht.yaj_rpc.net.zmq.RpcZmqClient;
import info.laht.yaj_rpc.net.zmq.RpcZmqServer;

import java.io.IOException;
import java.util.Scanner;

public class ZmqTest {

    public static void main(String[] args) throws IOException {
        RpcHandler handler = new RpcHandler(new SampleService());

        RpcServer server = new RpcZmqServer( handler);
        int port = server.start();

        AbstractRpcClient client = new RpcZmqClient("localhost", port);

        client.notify("SampleService.returnNothing", RpcParams.noParams());

        RpcParams params = RpcParams.listParams("Clint Eastwood");
        RpcResponse response = client.write("SampleService.greet", params);
        String result = response.getResult(String.class); //prints 'Hello Client Eastwood!'
        System.out.println("Response=" + result);

        System.out.println("Press any key to exit..");
        Scanner sc = new Scanner(System.in);
        if (sc.hasNext()) {
            System.out.println("exiting..");
        }

        client.close();
        server.close();
    }

}
