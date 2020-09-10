package cnabookstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

import java.util.Optional;


@RestController
 public class PointController {

 @Autowired
 PointRepository pointRepository;

 @GetMapping("/selectPointInfo")
 @HystrixCommand(fallbackMethod = "fallbackPoint", commandProperties = {
         @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000"),
         @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000")
 })
 public String selectPointInfo(@RequestParam long pointId) throws InterruptedException {

  if (pointId <= 0) {
   System.out.println("@@@ CircuitBreaker!!!");
   Thread.sleep(10000);
   //throw new RuntimeException("CircuitBreaker!!!");
  } else {
   Optional<Point> point = pointRepository.findById(pointId);
   return point.get().getPointValue().toString();
  }

  System.out.println("$$$ SUCCESS!!!");
  return " SUCCESS!!!";
 }

 private String fallbackPoint(long pointId) {
  System.out.println("### fallback!!!");
  return "CircuitBreaker!!!";
 }


 @GetMapping("/points/{pointId}")
 public Point queryPoint(@PathVariable("pointId") Long pointId) {
  Optional<Point> point = pointRepository.findById(pointId);
  return point.get();
 }

 }
