package cnabookstore;

import cnabookstore.config.kafka.KafkaProcessor;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_UsePoint(@Payload Ordered ordered){

        if(ordered.isMe()){
            System.out.println("##### listener UsePoint : " + ordered.toJson());
        }
    }

}
