package cnabookstore;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ZombieController {

    private boolean flag;



    public ZombieController(){
        flag = true;
    }

    @GetMapping({"/isHealthy"})
    public void zombie2() throws Exception {
        if (flag)
            return;
        else
            throw new Exception("zombie.....");
    }

    @GetMapping({"/makeZombie"})
    public void getStockInputs() {

        flag = false;

    }



}
