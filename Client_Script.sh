#!/bin/bash
echo
echo "##### CLIENT #####"
echo
echo "Inserire (intervallati da uno spazio):"
echo "- il percorso del file contenente le richieste"
echo "- l'indirizzo IP del server"
echo "- il numero di client_thread da creare"
echo "- 1 se si vuole tener conto del token immutable, 0 altrimenti"
echo
read a
java -Xms4096m -Xmx4096m -jar Client.jar $a
