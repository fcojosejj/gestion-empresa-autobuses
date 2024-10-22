package es.ujaen.ssccdd.curso2023_24;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static es.ujaen.ssccdd.curso2023_24.Constantes.NUM_PARADAS;
import static es.ujaen.ssccdd.curso2023_24.Constantes.TAM_AUTOBUS;

public class Monitor {
    private int[] capacidadBus;
    private int[] personasParada;
    private int[][] bajarParada;
    private int idBus;

    private final Lock lock;
    private Condition[] esperaParada;
    private Condition[][] esperaPasajeros;
    private Condition esperaAutobus;

    public Monitor(int numParadas, int numAutobuses) {
        this.capacidadBus = new int[numAutobuses];
        this.personasParada = new int[numParadas];
        this.bajarParada = new int[numAutobuses][numParadas];
        this.lock = new ReentrantLock();
        this.esperaParada = new Condition[numParadas];
        this.esperaPasajeros = new Condition[numAutobuses][numParadas];
        this.esperaAutobus = lock.newCondition();
        for (int i = 0; i < numParadas; i++) {
            this.personasParada[i] = 0;
            this.esperaParada[i] = lock.newCondition();
        }

        for (int i = 0; i < numAutobuses; i++) {
            this.capacidadBus[i] = 0;
            for (int j = 0; j < numParadas; j++) {
                this.bajarParada[i][j] = 0;
                this.esperaPasajeros[i][j] = lock.newCondition();
            }
        }
    }

    /**
     * Método que simula la espera de una persona en una parada.
     * La persona llega a la parada y espera a que llegue un autobús.
     */
    public int esperarParada(int parada) throws InterruptedException {
        lock.lock();

        personasParada[parada]++;
        esperaParada[parada].await();

        lock.unlock();
        return idBus;
    }

    /**
     * Método que simula la llegada de un autobús a una parada
     * Primero comprueba si alguien quiere bajar, sino se comprueba si alguien quiere subir y hay sitio, sino no hace nada.
     */
    public void llegadaAutobus(int idBus, int idParada) throws InterruptedException {
        lock.lock();

        this.idBus = idBus;
        if (bajarParada[idBus][idParada] > 0) {
            capacidadBus[idBus]--;
            bajarParada[idBus][idParada]--;
            esperaPasajeros[idBus][idParada].signal();
            esperaAutobus.await();
        } else if (personasParada[idParada] > 0 && capacidadBus[idBus] < TAM_AUTOBUS) {
            personasParada[idParada]--;
            esperaParada[idParada].signal();
            esperaAutobus.await();
        }

        lock.unlock();
    }

    /**
     * Método que simula la subida de una persona a un autobús.
     * La persona sube al autobús, comprueba si alguien más quiere subir y espera a llegar a su destino.
     * Cuando llegue a su destino, comprueba si alguien más quiere bajar, sino si alguien quiere subir, y si no el autobús avanza.
     */
    public void subidoAutobus(int paradaInicial, int paradaDestino) throws InterruptedException {
        lock.lock();

        capacidadBus[idBus]++;
        if (personasParada[paradaInicial] > 0 && capacidadBus[idBus] < TAM_AUTOBUS) {
            personasParada[paradaInicial]--;
            esperaParada[paradaInicial].signal();
        } else esperaAutobus.signal();

        bajarParada[idBus][paradaDestino]++;
        esperaPasajeros[idBus][paradaDestino].await();

        capacidadBus[idBus]--;
        if (bajarParada[idBus][paradaDestino] > 0) {
            bajarParada[idBus][paradaDestino]--;
            esperaPasajeros[idBus][paradaDestino].signal();
        } else if (personasParada[paradaDestino] > 0 && capacidadBus[idBus] < TAM_AUTOBUS) {
            personasParada[paradaDestino]--;
            esperaParada[paradaDestino].signal();
        } else esperaAutobus.signal();

        lock.unlock();
    }

    /**
     * Método que indica a un autobús cuál es su siguiente parada.
     */
    public int siguienteParada(int numParada) {
        lock.lock();
        numParada = (numParada + 1) % NUM_PARADAS;
        lock.unlock();
        return numParada;
    }
}
