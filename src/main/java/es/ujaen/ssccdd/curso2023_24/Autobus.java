package es.ujaen.ssccdd.curso2023_24;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static es.ujaen.ssccdd.curso2023_24.Constantes.NUM_PARADAS;
import static es.ujaen.ssccdd.curso2023_24.Constantes.TIEMPO_VIAJE;

public class Autobus implements Runnable{
    private final int id;
    private int siguienteParada;
    private final Monitor monitor;

    public Autobus(int id, Monitor monitor){
        this.id = id;
        this.monitor = monitor;
        this.siguienteParada = ThreadLocalRandom.current().nextInt(NUM_PARADAS - 1) + 1;
    }
    @Override
    public void run() {
        try{
            while(true){
                System.out.printf("Autobús %d en la parada %d\n", id, siguienteParada);
                monitor.llegadaAutobus(id, siguienteParada);
                System.out.printf("Autobús %d sale de la parada %d\n", id, siguienteParada);

                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(TIEMPO_VIAJE - 1) + 1);

                siguienteParada = monitor.siguienteParada(siguienteParada);
            }
        } catch (InterruptedException e){
            System.out.printf("Autobús %d interrumpido\n", id);
        }
    }
}
