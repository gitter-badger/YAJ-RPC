package info.laht.yaj_rpc;

import info.laht.yaj_rpc.net.AbstractAsyncRpcClient;
import info.laht.yaj_rpc.net.RpcServer;
import info.laht.yaj_rpc.net.ws.RpcWebSocketClient;
import info.laht.yaj_rpc.net.ws.RpcWebSocketServer;
import kotlin.Unit;

import java.util.Scanner;

class WebSocketDemo {

    public static void main(String[] args) throws Exception {

        RpcHandler handler = new RpcHandler(new SampleService());

        RpcServer server = new RpcWebSocketServer(handler);
        int port = server.start();

        AbstractAsyncRpcClient client = new RpcWebSocketClient("localhost", port);

        client.notify("SampleService.returnNothing", RpcParams.noParams());

        RpcParams params = RpcParams.listParams("Clint Eastwood");
        RpcResponse response = client.write("SampleService.greet", params);
        String result = response.getResult(String.class); //prints 'Hello Client Eastwood!'
        System.out.println("Response=" + result);

        client.writeAsync("SampleService.greet", params, res -> {
            System.out.println(res.getResult(String.class)); //prints 'Hello Client Eastwood!'
            return Unit.INSTANCE;
        });

        System.out.println("Press any key to exit..");
        Scanner sc = new Scanner(System.in);
        if (sc.hasNext()) {
            System.out.println("exiting..");
        }

        client.close();
        server.close();

    }

}
