package es.ujaen.ssccdd.curso2023_24;

import java.util.concurrent.ThreadLocalRandom;

public class Persona implements Runnable{
    public final int id;
    public int paradaInicial, paradaDestino, viajes, autobus;
    public final Monitor monitor;

    public Persona(int id, Monitor monitor){
        this.id = id;
        this.monitor = monitor;
        this.paradaInicial = ThreadLocalRandom.current().nextInt(Constantes.NUM_PARADAS - 1) + 1;
        this.paradaDestino = ThreadLocalRandom.current().nextInt(Constantes.NUM_PARADAS - 1) + 1;
        this.viajes = this.autobus = 0;
    }

    @Override
    public void run() {
        try {
            while (viajes < Constantes.NUM_VIAJES) {
                System.out.printf("Persona %d quiere ir de la parada %d a la parada %d\n", id, paradaInicial, paradaDestino);
                autobus = monitor.esperarParada(paradaInicial);

                System.out.printf("Persona %d sube al autobús %d en la parada %d\n", id, autobus, paradaInicial);
                monitor.subidoAutobus(paradaInicial, paradaDestino);

                System.out.printf("Persona %d baja del autobús %d en la parada %d\n", id, autobus, paradaDestino);

                paradaInicial = paradaDestino;
                paradaDestino = ThreadLocalRandom.current().nextInt(Constantes.NUM_PARADAS - 1) + 1;
                viajes++;
            }

            System.out.printf("Persona %d ha terminado sus viajes\n", id);
        } catch (InterruptedException e) {
            System.out.printf("Persona %d interrumpida\n", id);
        }
    }
}
