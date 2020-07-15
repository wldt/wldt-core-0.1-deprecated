package it.unimore.wldt.test.processing;

import it.unimore.dipi.iot.wldt.processing.PipelineData;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipeline;
import it.unimore.dipi.iot.wldt.processing.ProcessingPipelineListener;

import java.util.Optional;

public class ProcessingTester {

    public static void main(String[] args) {

       try{

           ProcessingPipeline defaultProcessingPipeline = new ProcessingPipeline();

           defaultProcessingPipeline.addStep(new TestProcessingStep());
           defaultProcessingPipeline.addStep(new TestProcessingStep());
           defaultProcessingPipeline.addStep(new TestProcessingStep());

           defaultProcessingPipeline.start(new TestPipelineData("PipelineTest"), new ProcessingPipelineListener() {

               @Override
               public void onPipelineDone(Optional<PipelineData> result) {

                   if(result.isPresent()){
                       System.out.println("Final Result: " + ((TestPipelineData)result.get()).getValue());
                   }
                   else{
                       System.out.println("The Pipeline produced an empty output !");
                   }

               }

               @Override
               public void onPipelineError() {
                   System.err.println("Pipeline Error !");
               }

           });

       }catch (Exception e){
           e.printStackTrace();
       }

    }
}
