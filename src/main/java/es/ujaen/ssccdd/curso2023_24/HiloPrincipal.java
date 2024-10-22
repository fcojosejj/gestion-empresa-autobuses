package es.ujaen.ssccdd.curso2023_24;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class HiloPrincipal {

    public static final int ESPERA_EJECUCION_PERSONAS = 500;
    public static final int ESPERA_FINALIZACION_MARCO = 1;

    public static void main(String[] args) throws InterruptedException {
        // Declaración de variables
        Persona personas[];
        Autobus autobuses[];
        Monitor monitor;
        ExecutorService marcoPersonas;
        ExecutorService marcoAutobuses;

        // Inicialización de variables
        monitor = new Monitor(Constantes.NUM_PARADAS, Constantes.NUM_AUTOBUSES);
        personas = new Persona[Constantes.NUM_PERSONAS];
        autobuses = new Autobus[Constantes.NUM_AUTOBUSES];
        marcoPersonas = (ExecutorService) Executors.newCachedThreadPool();
        marcoAutobuses = (ExecutorService) Executors.newCachedThreadPool();

        for (int i = 0; i < personas.length; i++) {
            personas[i] = new Persona(i, monitor);
        }

        for (int i = 0; i < autobuses.length; i++) {
            autobuses[i] = new Autobus(i, monitor);
        }

        // Cuerpo de ejecución
        System.out.printf("HILO Principal Generando autobuses\n");
        for (int i = 0; i < autobuses.length; i++) {
            marcoAutobuses.execute(autobuses[i]);
        }

        System.out.printf("HILO Principal Generando personas\n");
        for (int i = 0; i < personas.length; i++) {
            marcoPersonas.execute(personas[i]);

            // Las personas no llegan de golpe, van llegando poco a poco
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(ESPERA_EJECUCION_PERSONAS - 1) + 1);
        }

        marcoPersonas.shutdown();

        try {
            marcoPersonas.awaitTermination(ESPERA_FINALIZACION_MARCO, TimeUnit.DAYS);
            marcoPersonas.shutdownNow();
        } catch (InterruptedException e) {
            System.out.println("HILO Principal interrumpido");
        }

        marcoAutobuses.shutdownNow();
        try{
            marcoAutobuses.awaitTermination(ESPERA_FINALIZACION_MARCO, TimeUnit.DAYS);
        } catch (InterruptedException e){
            System.out.println("HILO Principal interrumpido");
        }
        // Finalización
        System.out.printf("HILO Principal Ha finalizado la ejecución\n");
    }
}