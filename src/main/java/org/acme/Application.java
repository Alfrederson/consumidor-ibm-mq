package org.acme;
import com.ibm.msg.client.commonservices.trace.Trace;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Application implements QuarkusApplication {
    public static volatile boolean finished = false;
    @Override
    public int run(String... args) throws Exception {
        System.out.println("Então vamo la!...");
        while(finished == false){
            Thread.sleep(1000);
        }
        return 0;
    }
}
