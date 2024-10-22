Este repositorio contiene mi solución a un proyecto de la asignatura Sistemas Concurrentes y Distribuidos del 2º curso de Ingeniería Informática de la Universidad de Jaén. Para la solución de la práctica se utilizará como herramienta de concurrencia el desarrollo de monitores, y también se utilizará la factoría [`Executors`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/Executors.html) y la interface [`ExecutorService`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ExecutorService.html) para la ejecución de las tareas concurrentes que compondrán la solución de la práctica.
## Problema a resolver
Hay que diseñar un monitor que lleva el control del sistema de trasporte de una línea de autobús urbano. Para el diseño del monitor tenemos en cuenta las siguientes variables:
 - El número de paradas de la línea estará definida por un número **P** de paradas.
 - La cantidad de autobuses que componen la línea, es decir, hay un número de **N** autobuses que estarán recorriendo la línea.
 - Las paradas de la línea representan un recorrido secuencial que deberá realizar el autobús y que la siguiente parada de la última es otra vez la primera de la línea, es decir, es un recorrido circular.
El monitor deberá controlar las siguientes acciones:
 - Que un usuario espere en una parada la llegada de un autobús para dirigirse a su parada de destino.
 - Que el usuario deberá montar en el autobús cuando llegue a la parada donde se encuentra esperando si hay sitio en el autobús.
 - Que el usuario deje el autobús cuando llegue a la parada de destino de su viaje.
 - Que un autobús llegue a una parada para dejar a los usuarios que se bajan en esa parada antes de dejar subir a los que estén esperando en esa parada.
 - La nueva parada que en la que deberá parar el autobús. Si el autobús ya está lleno parará en la primera parada donde se bajen usuarios dado que no podrá recoger más gente hasta entonces.
Los procesos que formarán parte de la solución son los siguientes:
 -  **Proceso usuario**, simulará las operaciones de un usuario con la línea de trasporte. Tendrá una parada de origen y una de destino que simulará un viaje para ese usuario.
 -  **Proceso autobús**, simulará el recorrido que debe hacer un autobús en la línea de trasporte. Desde que se inicia su ejecución estará recorriendo la línea de transporte hasta que finalice la jornada. Cuando finaliza la jornada el autobús dejará a todos los usuarios que aún quedan dentro del autobús antes de finalizar y volver a cocheras.
En la resolución del ejercicio hay que tener en cuenta las siguientes restricciones:
 - Siempre que haya usuarios que quieran bajar en una parada serán los primero en hacerlo antes de que nuevos usuarios puedan subir al autobús.
 - No se puede superar la capacidad máxima del autobús en el transporte de usuarios.
## Análisis y diseño
### Análisis
A continuación se van a describir todos los procesos y variables necesarias para el desarrollo de la solución de esta práctica.
#### Variables compartidas
En esta práctica el único elemento que se comparte por los procesos es el propio **monitor**.
#### Constantes
Algunas de las constantes más destacables que formarán parte de la solución del problema son:
- `NUM_PARADAS:` Número de paradas que conforman la línea de autobuses.
- `NUM_AUTOBUSES:` Número de autobuses que trabajan para la línea.
- `NUM_PERSONAS:` Número de personas que irán llegando a las diferentes paradas de la línea.
- `TAM_AUTOBUS:` Tamaño máximo de un autobús, o máximo de personas que pueden subirse a un autobús.
#### Monitor
Para el monitor se utilizará la estructura [`Lock`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/locks/Lock.html) para la exclusión mutua, a la cual se le puede asignar condiciones [`Condition`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/locks/Condition.html) para simular correctamente el monitor visto en teoría. El monitor contará con todas las variables necesarias para su correcto funcionamiento.
### Diseño
A continuación se mostrará en pseudocódigo el funcionamiento del proceso `Monitor`, y posteriormente de los procesos `Persona`, `Autobus`  y del `HiloPrincipal`.
#### Monitor
```
Variables Monitor:
	// Las principales variables que vamos a usar, posteriormente pueden aparecer más
	capacidadBus[id]: entero // Capacidad actual de cada autobús
	personasParada[id]: entero // Personas esperando en cada parada
	bajarParada[id]: entero // Persona que quieren bajarse en una parada
	
	esperaParada[id]: condicion // Para que las personas esperen en una parada
	esperaPasajeros[id]: condicion // Para que las personas esperen dentro de un autobús
	esperaAutobus: condicion // Para bloquear al autobús cuando bajen y suban personas
	
Procesos Monitor:
	esperarParada(), llegadaAutobus(), subidoAutobus(), siguienteParada()


esperarParada(idParada){
	// Espero al autobús
	personasParada[idParada]++
	delay(esperaParada[idParada])
}

llegadaAutobus(autobus, idBus, idParada){
	Si autobus NO lleno y NO hay otro en esa parada
		Si quieren bajar personas
			capacidadBus[idBus]--
			bajarParada[idParada]--
			resume(esperaPasajeros[idBus])
			delay(esperaAutobus)
		Si no
			Si quieren subir personas
				personasParada[idParada]--
				resume(esperaParada[idParada])
				delay(esperaAutobus)
		
}

subidoAutobus(paradaInicial, paradaDestino){
	// Se suben personas al autobús
	capacidadBus[idBus]++
	Si hay personas para subir y autobus no lleno
		personasParada[paradaInicial]--
		resume(esperaParada[paradaInicial])
	Si no
		// Si nadie más se sube el autobús continúa
		resume(esperaAutobus)

	// Nos "sentamos" dentro del autobús
	bajarParada[paradaDestino]++
	delay(esperaPasajeros[idBus])

	// Nos bajamos del autobús
	capacidadBus[idBus]--
	Si quieren bajar personas
		bajarParada[paradaDestino]--
		resume(esperaPasajeros[idBus])
	Si no
		Si quieren subir personas
			personasParada[idParada]--
			resume(esperaParada[idParada])
		Si no
			resume(esperaAutobus)
}

entero: siguienteParada(numParada){
	numParada = numParada + 1 mod NUM_PARADAS
	return numParada
}
```
#### Persona
```
Variables locales:
	paradaInicial: entero // Parada a la que llegue la persona
	paradaDestino: entero // Parada de destino de la persona
	viajes: entero // Número de viajes que hará la persona antes de irse

ejecucion(){
	Mientras viajes < MAX_VIAJES
		monitor.esperarParada(paradaInicial)
		monitor.subidoAutobus(paradaInicial, paradaDestino)	
		viajes++
		paradaInicial = paradaDestino
		random(paradaDestino)
}
```
#### Autobús
```
Variables locales:
	siguienteParada: entero // Parada donde se dirige el autobús

ejecucion(){
	Mientras no interrumpido
		monitor.llegadaAutobus(this, id, siguienteParada)
		siguienteParada = monitor.siguienteParada(siguienteParada)
}
```
#### Hilo principal
```
crearMonitor()
Para numPersonas
	crearPersona()
	ejecutarPersona(persona)

Para numAutobuses
	crearAutobus()
	ejecutarAutobus(autobus)

pararEjecucion()
```
## Modificaciones realizadas
Durante la implementación de la práctica, se han detectado y corregido los siguientes errores:
* Las variables `bajarParada` y `esperaPasajeros` no se estaban inicializando correctamente. En lugar de usar un único array para todos los autobuses, es mejor utilizar un array bidimensional, de modo que cada autobús tiene su propio array.
* Se ha modificado el orden de las comprobaciones del método `llegadaAutobus`, de modo que ahora comprueba primero de todo si alguien se quiere bajar, y si no comprueba si alguien quiere subir y hay sitio en el autobús, tal y como se pide en el enunciado de la práctica.
* El método `esperarParada` ahora devuelve el autobús al que la persona se ha subido para mostrarlo por consola.
* Ahora los procesos `Autobus` simulan un tiempo de viaje entre paradas. 
