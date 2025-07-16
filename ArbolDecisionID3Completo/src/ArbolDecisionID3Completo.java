import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class ArbolDecisionID3Completo extends JFrame {

    static class Instancia {
        String estado;
        int temperatura;
        int humedad;
        String viento;
        String clase;

        Instancia(String estado, int temperatura, int humedad, String viento, String clase) {
            this.estado = estado;
            this.temperatura = temperatura;
            this.humedad = humedad;
            this.viento = viento;
            this.clase = clase;
        }
    }

    static class Nodo {
        String texto;
        int x, y;
        List<Nodo> hijos = new ArrayList<>();
        Color colorFondo;
        Color colorTexto;

        Nodo(String texto) {
            this(texto, Color.WHITE, Color.BLACK);
        }

        Nodo(String texto, Color fondo, Color textoColor) {
            this.texto = texto;
            this.colorFondo = fondo;
            this.colorTexto = textoColor;
        }

        void agregarHijo(Nodo hijo) {
            hijos.add(hijo);
        }
    }

    static class ArbolPanel extends JPanel {
        Nodo raiz;
        int nodoAncho = 80;
        int nodoAlto = 40;
        int separacionY = 80;
        int margenX = 10;
        Map<Nodo, Integer> anchuraSubarbol = new HashMap<>();
        Stroke lineaStroke = new BasicStroke(2.0f);

        public ArbolPanel(Nodo raiz) {
            this.raiz = raiz;
            setPreferredSize(new Dimension(2000, 1500));
            setBackground(new Color(245, 245, 245));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            anchuraSubarbol.clear();
            calcularAnchura(raiz);
            asignarPosiciones(raiz, getWidth() / 2, 50);
            dibujar(g, raiz);
        }

        private int calcularAnchura(Nodo nodo) {
            if (nodo.hijos.isEmpty()) {
                anchuraSubarbol.put(nodo, nodoAncho + margenX);
                return nodoAncho + margenX;
            }
            int ancho = 0;
            for (Nodo hijo : nodo.hijos) ancho += calcularAnchura(hijo);
            anchuraSubarbol.put(nodo, ancho);
            return ancho;
        }

        private void asignarPosiciones(Nodo nodo, int centroX, int nivel) {
            nodo.y = nivel;
            int anchoTotal = anchuraSubarbol.get(nodo);
            int inicioX = centroX - anchoTotal / 2;
            int acumuladoX = inicioX;
            for (Nodo hijo : nodo.hijos) {
                int anchoHijo = anchuraSubarbol.get(hijo);
                int hijoCentroX = acumuladoX + anchoHijo / 2;
                asignarPosiciones(hijo, hijoCentroX, nivel + separacionY);
                acumuladoX += anchoHijo;
            }
            nodo.x = centroX;
        }

        private void dibujar(Graphics g, Nodo nodo) {
            g.setColor(nodo.colorFondo);
            g.fillRoundRect(nodo.x - nodoAncho / 2, nodo.y, nodoAncho, nodoAlto, 15, 15);

            g.setColor(Color.BLACK);
            ((Graphics2D) g).setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(nodo.x - nodoAncho / 2, nodo.y, nodoAncho, nodoAlto, 15, 15);

            g.setColor(nodo.colorTexto);
            g.setFont(new Font("Arial", Font.BOLD, 11));
            drawTextoCentrado(g, nodo.texto, nodo.x, nodo.y, nodoAncho, nodoAlto);

            Stroke originalStroke = ((Graphics2D) g).getStroke();
            ((Graphics2D) g).setStroke(lineaStroke);
            for (Nodo hijo : nodo.hijos) {
                g.setColor(new Color(70, 70, 70));
                g.drawLine(nodo.x, nodo.y + nodoAlto, hijo.x, hijo.y);
                dibujar(g, hijo);
            }
            ((Graphics2D) g).setStroke(originalStroke);
        }

        private void drawTextoCentrado(Graphics g, String texto, int x, int y, int w, int h) {
            FontMetrics fm = g.getFontMetrics();
            String[] lineas = texto.split("\\n");
            int alturaTotal = lineas.length * fm.getHeight();
            int yInicio = y + (h - alturaTotal) / 2 + fm.getAscent();
            for (String linea : lineas) {
                int anchoLinea = fm.stringWidth(linea);
                int xInicio = x - anchoLinea / 2;
                g.drawString(linea, xInicio, yInicio);
                yInicio += fm.getHeight();
            }
        }
    }

    private JTextArea resultadosArea;
    private JTable tablaDatos;
    private DefaultTableModel tableModel;
    private ArbolPanel panelArbol;
    private List<Instancia> datosOriginales;

    public ArbolDecisionID3Completo(Nodo raiz, List<Instancia> datos) {
    this.datosOriginales = new ArrayList<>();
    for (Instancia instancia : datos) {
        this.datosOriginales.add(new Instancia(
            instancia.estado,
            instancia.temperatura,
            instancia.humedad,
            instancia.viento,
            instancia.clase
        ));
    }

        setTitle("Árbol de Decisión ID3 con Resultados y Tabla");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);

        JSplitPane splitPanePrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPanePrincipal.setDividerLocation(1000);

        panelArbol = new ArbolPanel(raiz);
        JScrollPane scrollArbol = new JScrollPane(panelArbol);
        splitPanePrincipal.setLeftComponent(scrollArbol);

        JSplitPane splitPaneDerecha = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPaneDerecha.setDividerLocation(400);

        resultadosArea = new JTextArea();
        resultadosArea.setEditable(false);
        resultadosArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultadosArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollResultados = new JScrollPane(resultadosArea);

        String[] columnNames = {"Estado", "Temperatura", "Humedad", "Viento", "Clase"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        for (Instancia instancia : datos) {
            tableModel.addRow(new Object[]{
                instancia.estado,
                instancia.temperatura,
                instancia.humedad,
                instancia.viento,
                instancia.clase
            });
        }

        tablaDatos = new JTable(tableModel);
        tablaDatos.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaDatos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane scrollTabla = new JScrollPane(tablaDatos);

        splitPaneDerecha.setTopComponent(scrollResultados);
        splitPaneDerecha.setBottomComponent(scrollTabla);

        splitPanePrincipal.setRightComponent(splitPaneDerecha);
        add(splitPanePrincipal, BorderLayout.CENTER);

        JPanel panelBoton = new JPanel(new FlowLayout());

        JButton btnCalcular = new JButton("Calcular Ganancias");
        btnCalcular.addActionListener(e -> calcularGananciasYMostrar(convertirDatosTabla()));

        JButton btnActualizarArbol = new JButton("Actualizar Árbol");
        btnActualizarArbol.addActionListener(e -> actualizarArbol());

        JButton btnRestaurar = new JButton("Restaurar Original");
        btnRestaurar.addActionListener(e -> restaurarOriginal());

        panelBoton.add(btnCalcular);
        panelBoton.add(btnActualizarArbol);
        panelBoton.add(btnRestaurar);
        add(panelBoton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private List<Instancia> convertirDatosTabla() {
        List<Instancia> nuevosDatos = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                Instancia instancia = new Instancia(
                    tableModel.getValueAt(i, 0).toString(),
                    Integer.parseInt(tableModel.getValueAt(i, 1).toString()),
                    Integer.parseInt(tableModel.getValueAt(i, 2).toString()),
                    tableModel.getValueAt(i, 3).toString(),
                    tableModel.getValueAt(i, 4).toString()
                );
                nuevosDatos.add(instancia);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error en la fila " + (i+1), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return nuevosDatos;
    }

private void actualizarArbol() {
    if (!hayCambiosEnTabla()) {
        JOptionPane.showMessageDialog(this, 
            "No hay cambios en la tabla para actualizar el árbol.", 
            "Sin cambios", 
            JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    List<Instancia> nuevosDatos = convertirDatosTabla();
    if (!nuevosDatos.isEmpty()) {
        // Elimina esta línea: datosOriginales = new ArrayList<>(nuevosDatos);
        Nodo nuevoArbol = generarArbolID3(nuevosDatos);
        panelArbol.raiz = nuevoArbol;
        panelArbol.repaint();
        JOptionPane.showMessageDialog(this, 
            "Árbol actualizado con los datos modificados.", 
            "Actualizado", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}
private boolean hayCambiosEnTabla() {
    List<Instancia> datosActuales = convertirDatosTabla();
    if (datosActuales.size() != datosOriginales.size()) {
        return true;
    }
    
    for (int i = 0; i < datosActuales.size(); i++) {
        Instancia actual = datosActuales.get(i);
        Instancia original = datosOriginales.get(i);
        
        if (!actual.estado.equals(original.estado) ||
            actual.temperatura != original.temperatura ||
            actual.humedad != original.humedad ||
            !actual.viento.equals(original.viento) ||
            !actual.clase.equals(original.clase)) {
            return true;
        }
    }
    return false;
}

private void restaurarOriginal() {
    // Limpiar la tabla completamente
    tableModel.setRowCount(0);
    
    // Restaurar cada instancia original en la tabla
    for (Instancia instancia : datosOriginales) {
        tableModel.addRow(new Object[]{
            instancia.estado,
            instancia.temperatura,
            instancia.humedad,
            instancia.viento,
            instancia.clase
        });
    }
    
    // Reconstruir el árbol original
    Nodo arbolOriginal = construirArbol();
    panelArbol.raiz = arbolOriginal;
    panelArbol.repaint();
    
    // Limpiar resultados
    resultadosArea.setText("");
    
    // Mostrar confirmación
    JOptionPane.showMessageDialog(this, 
        "Datos y árbol restaurados a los valores originales", 
        "Restauración completada", 
        JOptionPane.INFORMATION_MESSAGE);
}

private void calcularGananciasYMostrar(List<Instancia> datos) {
    resultadosArea.setText("");
    resultadosArea.append("====== Análisis de atributos con entropia ======\n\n");
    
    // Calcular entropía inicial
    double entropiaInicial = calcularEntropia(datos);
    resultadosArea.append("--- Calculo de entropia inicial ---\n");
    resultadosArea.append("Donde:\nP = Probabilidad de ocurrencia de la clase\n");
    resultadosArea.append("H = -P*log2(P), medida de incertidumbre por clase\n");
    
    Map<String, Integer> conteoClases = new HashMap<>();
    for (Instancia i : datos) {
        conteoClases.put(i.clase, conteoClases.getOrDefault(i.clase, 0) + 1);
    }
    
    int totalDatos = datos.size();
    for (Map.Entry<String, Integer> entry : conteoClases.entrySet()) {
        double probabilidad = (double) entry.getValue() / totalDatos;
        double entropiaClase = -probabilidad * (Math.log(probabilidad) / Math.log(2));
        resultadosArea.append(String.format("Clase: %s | P = %.4f | H = %.4f\n", 
            entry.getKey(), probabilidad, entropiaClase));
    }
    resultadosArea.append(String.format("Entropia total del conjunto: %.4f\n\n", entropiaInicial));
    
    // Calcular ganancia para cada atributo
    for (String atributo : List.of("estado", "temperatura", "humedad", "viento")) {
        resultadosArea.append("=== Calculando ganancia para atributo '" + atributo + "' ===\n");
        
        Map<String, List<Instancia>> particiones = new HashMap<>();
        for (Instancia i : datos) {
            String key = switch (atributo) {
                case "estado" -> i.estado;
                case "viento" -> i.viento;
                case "temperatura" -> i.temperatura >= 76 ? ">=76" : "<76";
                case "humedad" -> i.humedad >= 82 ? ">=82" : "<82";
                default -> "";
            };
            particiones.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }
        
        double entropiaPonderada = 0.0;
        
        for (Map.Entry<String, List<Instancia>> entry : particiones.entrySet()) {
            String valor = entry.getKey();
            List<Instancia> subset = entry.getValue();
            double peso = (double) subset.size() / datos.size();
            double entropiaSubset = calcularEntropia(subset);
            
            resultadosArea.append("\n--- Calculo de entropia para " + atributo + " = " + valor + " ---\n");
            resultadosArea.append("Donde:\nP = Probabilidad de ocurrencia de la clase\n");
            resultadosArea.append("H = -P*log2(P), medida de incertidumbre por clase\n");
            
            Map<String, Integer> conteoSubset = new HashMap<>();
            for (Instancia i : subset) {
                conteoSubset.put(i.clase, conteoSubset.getOrDefault(i.clase, 0) + 1);
            }
            
            int totalSubset = subset.size();
            for (Map.Entry<String, Integer> e : conteoSubset.entrySet()) {
                double probabilidad = (double) e.getValue() / totalSubset;
                double entropiaClase = -probabilidad * (Math.log(probabilidad) / Math.log(2));
                resultadosArea.append(String.format("Clase: %s | P = %.4f | H = %.4f\n", 
                    e.getKey(), probabilidad, entropiaClase));
            }
            resultadosArea.append(String.format("Entropia total del conjunto: %.4f\n", entropiaSubset));
            
            resultadosArea.append(String.format(
                "%s = %s | H = %.4f | peso = %.4f | peso*H = %.4f\n\n",
                atributo, valor, entropiaSubset, peso, peso * entropiaSubset
            ));
            
            entropiaPonderada += peso * entropiaSubset;
        }
        
        double ganancia = entropiaInicial - entropiaPonderada;
        resultadosArea.append(String.format(
            "Ganancia del atributo '%s': %.4f\n\n", atributo, ganancia
        ));
    }
}

    private double calcularEntropia(List<Instancia> datos) {
        int total = datos.size();
        Map<String, Integer> conteo = new HashMap<>();
        for (Instancia i : datos) conteo.put(i.clase, conteo.getOrDefault(i.clase, 0) + 1);
        double entropia = 0.0;
        for (int count : conteo.values()) {
            double p = (double) count / total;
            entropia -= p * (Math.log(p) / Math.log(2));
        }
        return entropia;
    }

    private double calcularGanancia(List<Instancia> datos, String atributo) {
        double entropiaInicial = calcularEntropia(datos);
        Map<String, List<Instancia>> particiones = new HashMap<>();
        for (Instancia i : datos) {
            String key = switch (atributo) {
                case "estado" -> i.estado;
                case "viento" -> i.viento;
                case "temperatura" -> i.temperatura >= 76 ? ">=76" : "<76";
                case "humedad" -> i.humedad >= 82 ? ">=82" : "<82";
                default -> "";
            };
            particiones.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }
        double entropiaPonderada = 0.0;
        for (List<Instancia> subset : particiones.values()) {
            double peso = (double) subset.size() / datos.size();
            entropiaPonderada += peso * calcularEntropia(subset);
        }
        return entropiaInicial - entropiaPonderada;
    }

    private Nodo generarArbolID3(List<Instancia> datos) {
        return generarArbolRecursivo(datos, new HashSet<>(List.of("estado", "temperatura", "humedad", "viento")));
    }

    private Nodo generarArbolRecursivo(List<Instancia> datos, Set<String> atributosDisponibles) {
        if (datos.isEmpty()) return new Nodo("Sin datos", Color.LIGHT_GRAY, Color.BLACK);
        String claseComun = datos.get(0).clase;
        boolean mismaClase = datos.stream().allMatch(i -> i.clase.equals(claseComun));
        if (mismaClase) {
            Color colorHoja = claseComun.equalsIgnoreCase("jugar") ? new Color(200, 255, 200) : new Color(255, 200, 200);
            return new Nodo(claseComun.equalsIgnoreCase("jugar") ? "J" : "NJ", colorHoja, Color.BLACK);
        }
        if (atributosDisponibles.isEmpty()) {
            Map<String, Long> conteo = datos.stream().collect(
                java.util.stream.Collectors.groupingBy(i -> i.clase, java.util.stream.Collectors.counting()));
            String mayor = conteo.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
            return new Nodo(mayor.equalsIgnoreCase("jugar") ? "J" : "NJ", 
                            mayor.equalsIgnoreCase("jugar") ? new Color(200, 255, 200) : new Color(255, 200, 200), 
                            Color.BLACK);
        }

        String mejorAtributo = atributosDisponibles.stream()
            .max(Comparator.comparingDouble(attr -> calcularGanancia(datos, attr)))
            .orElse(null);

        Nodo nodo = new Nodo(mejorAtributo, new Color(220, 255, 220), Color.BLACK);
        Map<String, List<Instancia>> particiones = new HashMap<>();

        for (Instancia i : datos) {
            String key = switch (mejorAtributo) {
                case "estado" -> i.estado;
                case "viento" -> i.viento;
                case "temperatura" -> i.temperatura >= 76 ? ">=76" : "<76";
                case "humedad" -> i.humedad >= 82 ? ">=82" : "<82";
                default -> "";
            };
            particiones.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        Set<String> nuevosAtributos = new HashSet<>(atributosDisponibles);
        nuevosAtributos.remove(mejorAtributo);

        for (Map.Entry<String, List<Instancia>> entry : particiones.entrySet()) {
            Nodo valorNodo = new Nodo(entry.getKey(), new Color(255, 255, 180), Color.BLACK);
            valorNodo.agregarHijo(generarArbolRecursivo(entry.getValue(), nuevosAtributos));
            nodo.agregarHijo(valorNodo);
        }

        return nodo;
    }

    private static Nodo construirArbol() {
    Color colorRaiz = new Color(180, 220, 255);
    Color colorAtributo = new Color(220, 255, 220);
    Color colorValor = new Color(255, 255, 180);
    Color colorHojaJugar = new Color(200, 255, 200);
    Color colorHojaNoJugar = new Color(255, 200, 200);

    Nodo raiz = new Nodo("Estado general", colorRaiz, Color.BLACK);

    Nodo soleado = new Nodo("Soleado\n(0.97)", colorValor, Color.BLACK);
    Nodo nublado = new Nodo("Nublado\n(0)", colorValor, Color.BLACK);
    Nodo lluvioso = new Nodo("Lluvioso\n(0.97)", colorValor, Color.BLACK);
    raiz.agregarHijo(soleado);
    raiz.agregarHijo(nublado);
    raiz.agregarHijo(lluvioso);

    Nodo temp = new Nodo("Temperatura\n(0.54)", colorAtributo, Color.BLACK);
    Nodo hum = new Nodo("Humedad\n(0)", colorAtributo, Color.BLACK);
    Nodo viento = new Nodo("Viento\n(0.95)", colorAtributo, Color.BLACK);
    soleado.agregarHijo(temp);
    soleado.agregarHijo(hum);
    soleado.agregarHijo(viento);

    Nodo temp_ge76 = new Nodo(">=76\n(0)", colorValor, Color.BLACK);
    Nodo temp_lt76 = new Nodo("<76\n(0.54)", colorValor, Color.BLACK);
    temp.agregarHijo(temp_ge76);
    temp.agregarHijo(temp_lt76);
    temp_ge76.agregarHijo(new Nodo("NJ", colorHojaNoJugar, Color.BLACK));

    Nodo hum_ge82 = new Nodo(">=82\n(0)", colorValor, Color.BLACK);
    Nodo hum_lt82 = new Nodo("<82\n(0)", colorValor, Color.BLACK);
    hum.agregarHijo(hum_ge82);
    hum.agregarHijo(hum_lt82);
    hum_ge82.agregarHijo(new Nodo("NJ", colorHojaNoJugar, Color.BLACK));
    hum_lt82.agregarHijo(new Nodo("J", colorHojaJugar, Color.BLACK));

    Nodo viento_si = new Nodo("Si\n(1)", colorValor, Color.BLACK);
    Nodo viento_no = new Nodo("No\n(0.91)", colorValor, Color.BLACK);
    viento.agregarHijo(viento_si);
    viento.agregarHijo(viento_no);

    nublado.agregarHijo(new Nodo("J", colorHojaJugar, Color.BLACK));

    Nodo lviento = new Nodo("Viento\n(0)", colorAtributo, Color.BLACK);
    Nodo lhum = new Nodo("Humedad\n(0.8)", colorAtributo, Color.BLACK);
    Nodo ltemp = new Nodo("Temperatura\n(0.94)", colorAtributo, Color.BLACK);
    lluvioso.agregarHijo(lviento);
    lluvioso.agregarHijo(lhum);
    lluvioso.agregarHijo(ltemp);

    Nodo lv_si = new Nodo("Si\n(0)", colorValor, Color.BLACK);
    Nodo lv_no = new Nodo("No\n(0)", colorValor, Color.BLACK);
    lviento.agregarHijo(lv_si);
    lviento.agregarHijo(lv_no);
    lv_si.agregarHijo(new Nodo("NJ", colorHojaNoJugar, Color.BLACK));
    lv_no.agregarHijo(new Nodo("J", colorHojaJugar, Color.BLACK));

    Nodo lh_ge81 = new Nodo(">=81\n(0)", colorValor, Color.BLACK);
    Nodo lh_lt81 = new Nodo("<81\n(1)", colorValor, Color.BLACK);
    lhum.agregarHijo(lh_ge81);
    lhum.agregarHijo(lh_lt81);
    lh_ge81.agregarHijo(new Nodo("J", colorHojaJugar, Color.BLACK));

    Nodo lh_viento = new Nodo("Viento\n(0)", colorAtributo, Color.BLACK);
    Nodo lh_temp = new Nodo("Temperatura\n(1)", colorAtributo, Color.BLACK);
    lh_lt81.agregarHijo(lh_viento);
    lh_lt81.agregarHijo(lh_temp);

    Nodo lhv_si = new Nodo("Si\n(0)", colorValor, Color.BLACK);
    Nodo lhv_no = new Nodo("No\n(0)", colorValor, Color.BLACK);
    lh_viento.agregarHijo(lhv_si);
    lh_viento.agregarHijo(lhv_no);
    lhv_si.agregarHijo(new Nodo("NJ", colorHojaNoJugar, Color.BLACK));
    lhv_no.agregarHijo(new Nodo("J", colorHojaJugar, Color.BLACK));

    Nodo lht_ge70 = new Nodo(">=70\n(1)", colorValor, Color.BLACK);
    Nodo lht_lt70 = new Nodo("<70\n(1)", colorValor, Color.BLACK);
    lh_temp.agregarHijo(lht_ge70);
    lh_temp.agregarHijo(lht_lt70);

    Nodo lht_viento = new Nodo("Viento\n(0)", colorAtributo, Color.BLACK);
    lht_ge70.agregarHijo(lht_viento);
    Nodo lhtv_si = new Nodo("Si\n(0)", colorValor, Color.BLACK);
    Nodo lhtv_no = new Nodo("No\n(0)", colorValor, Color.BLACK);
    lht_viento.agregarHijo(lhtv_si);
    lht_viento.agregarHijo(lhtv_no);
    lhtv_si.agregarHijo(new Nodo("NJ", colorHojaNoJugar, Color.BLACK));
    lhtv_no.agregarHijo(new Nodo("J", colorHojaJugar, Color.BLACK));

    Nodo lt_ge70 = new Nodo(">=70\n(0.91)", colorValor, Color.BLACK);
    Nodo lt_lt70 = new Nodo("<70\n(1)", colorValor, Color.BLACK);
    ltemp.agregarHijo(lt_ge70);
    ltemp.agregarHijo(lt_lt70);

    return raiz;
}


    public static void main(String[] args) {
        List<Instancia> datos = Arrays.asList(
            new Instancia("soleado", 85, 85, "no", "NO jugar"),
            new Instancia("soleado", 80, 90, "si", "NO jugar"),
            new Instancia("nublado", 83, 78, "no", "jugar"),
            new Instancia("lluvioso", 70, 96, "no", "jugar"),
            new Instancia("lluvioso", 68, 80, "no", "jugar"),
            new Instancia("lluvioso", 65, 70, "si", "NO jugar"),
            new Instancia("nublado", 64, 65, "si", "jugar"),
            new Instancia("soleado", 72, 95, "no", "NO jugar"),
            new Instancia("soleado", 69, 70, "no", "jugar"),
            new Instancia("lluvioso", 75, 80, "no", "jugar"),
            new Instancia("soleado", 75, 70, "si", "jugar"),
            new Instancia("nublado", 72, 90, "si", "jugar"),
            new Instancia("nublado", 81, 75, "no", "jugar"),
            new Instancia("lluvioso", 71, 80, "si", "NO jugar")
        );

        Nodo arbol = construirArbol();

        SwingUtilities.invokeLater(() -> {
            ArbolDecisionID3Completo frame = new ArbolDecisionID3Completo(arbol, datos);
            frame.setVisible(true);
        });
    }
}
