package com.example.battleship.views;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

/**
 * Clase encargada de dibujar el efecto de humo utilizando JavaFX Canvas.
 * Se utiliza cuando un barco ha sido "Tocado" (HIT).
 */
public class CanvasSmokeRenderer {

    /**
     * Dibuja una nube de humo en el canvas proporcionado.
     * Utiliza elipses superpuestas con degradados radiales para simular volumen y transparencia.
     * @param canvas El lienzo donde se dibujará el humo.
     */
    public void draw(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Limpia el canvas antes de.
        gc.clearRect(0, 0, w, h);

        // Colore para el fuego
        Color fireCore = Color.rgb(255, 200, 50, 0.9);  // Amarillo naranja brillante centro
        Color fireMid = Color.rgb(255, 100, 0, 0.8);    // Naranja intenso
        Color fireEdge = Color.rgb(200, 50, 0, 0.0);    // Rojo transparente borde

        // Colores para el humo
        Color ashCore = Color.rgb(40, 40, 40, 0.95);   // Gris casi negro denso
        Color ashMid = Color.rgb(80, 80, 80, 0.8);     // Gris medio
        Color ashGrey = Color.rgb(150, 150, 150, 0.5); // Gris ceniza claro
        Color transparent = Color.TRANSPARENT;

        // Nube Base
        // Se usa un degradado radial para darle aspecto esponjoso
        // Foco principal de fuego en el centro inferior
        RadialGradient fireBaseGrad = new RadialGradient(
                0, 0, 0.5, 0.7, 0.4, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, fireCore),
                new Stop(0.5, fireMid),
                new Stop(1.0, fireEdge)
        );
        gc.setFill(fireBaseGrad);
        gc.fillOval(w * 0.1, h * 0.4, w * 0.8, h * 0.6);

        RadialGradient fireSpotGrad = new RadialGradient(
                0, 0, 0.3, 0.6, 0.25, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(255, 80, 0, 0.8)),
                new Stop(1.0, fireEdge)
        );
        gc.setFill(fireSpotGrad);
        gc.fillOval(w * 0.05, h * 0.45, w * 0.5, h * 0.5);


        // HUMO DE CENIZA (Se dibuja encima, tapando parcialmente el fuego)

        // Nube principal densa (Cuerpo central)
        // Usamos un gris muy oscuro para que parezca ceniza pesada
        RadialGradient ashMainGrad = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, ashCore), // Centro denso que tapa la bomba
                new Stop(0.7, ashMid),
                new Stop(1.0, transparent)
        );
        gc.setFill(ashMainGrad);
        gc.fillOval(w * 0.05, h * 0.1, w * 0.9, h * 0.8);

        // Nube superior
        RadialGradient ashTopGrad = new RadialGradient(
                0, 0, 0.5, 0.2, 0.4, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, ashGrey),
                new Stop(1.0, transparent)
        );
        gc.setFill(ashTopGrad);
        gc.fillOval(w * 0.2, h * -0.1, w * 0.6, h * 0.5);

        // Detalle lateral de ceniza
        RadialGradient ashSideGrad = new RadialGradient(
                0, 0, 0.8, 0.4, 0.3, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(60, 60, 60, 0.8)),
                new Stop(1.0, transparent)
        );
        gc.setFill(ashSideGrad);
        gc.fillOval(w * 0.6, h * 0.2, w * 0.4, h * 0.5);
    }
}