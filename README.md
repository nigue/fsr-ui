# fsr-ui

### Comunicación básica con un dispositivo USB (ttyACM)

Este documento describe tres comandos básicos para **identificar** y **comunicarse** con un dispositivo conectado vía USB que expone un puerto serie (`/dev/ttyACM0`).

Los ejemplos son útiles para pruebas rápidas sin depender de IDEs o herramientas gráficas.

---

##### 1. Identificar la ruta del dispositivo con `udevadm`

```bash
udevadm info -q path -n /dev/ttyACM0
```
```bash
echo -n "v" > /dev/ttyACM0
```
```bash
cat /dev/ttyACM0