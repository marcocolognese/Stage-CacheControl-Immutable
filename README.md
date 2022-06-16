# Cache-Control Immutable
Gli sviluppatori di Facebook portarono al “IETF HTTP Working Group” un problema che stavano riscontrando: una grande quantità (circa il 20%) del traffico di dati verso i loro server era rappresentato da richieste di aggiornamento per risorse con un lungo “cache lifetime” ed il numero era destinano a crescere. \
\
Queste richieste, poiché riguardano risorse che non subiscono modifiche per periodi molto lunghi, vengono sempre liquidate con un “304 - Not Modified”. \
\
Ciò rappresenta un inutile aumento di traffico nella rete e uno spreco di tempo per client e server.

### La soluzione ideale

Tale problema è facilmente riscontrabile su siti quali social network (es. Facebook) o siti di news in tempo reale, in cui si aggiorna spesso la pagina per avere sempre nuovi contenuti; però vengono aggiornate anche risorse “statiche” come i loghi del sito. \
\
La soluzione ideale, per evitare questo inutile spreco, consiste nel mandare richieste solamente per le risorse dinamiche, tralasciando quelle che non subiranno modifiche per periodi di tempo molto lunghi.


## L'estensione Immutable
Per implementare questa soluzione si è pensato di aggiungere un token nel campo Cache-Control dell’header Http: immutable (inizialmente chiamato never-revalidate). \
\
Questa modifica (presente solamente da Firefox 49) ha un grosso vantaggio: può essere attuata senza andare a modificare le cache già esistenti poiché è un’estensione dell’informazione, non del funzionamento. Infatti tale token deve essere considerato solo dalle cache che lo riconoscono, altrimenti viene semplicemente ignorato.

### Il suo utilizzo
Questa estensione deve essere applicata a tutte quelle risorse che, a detta dei server administrator, non subiranno mai modifiche (o almeno non le subiranno per tempi molto lunghi). \
\
Una cache che si trova di fronte ad una richiesta per una risorsa immutable, deve mandare al client la copia in essa salvata senza inviare mai nessuna richiesta al server per verificare che essa sia aggiornata. \
\
Per trarne effettivamente dei vantaggi, però, è necessario che tale token non venga assegnato a risorse con un “max-age” troppo breve, altrimenti è molto probabile che si verifichi una inconsistenza di informazioni. \
L’implementazione di questo token richiede delle modifiche lato client e server:
- i client devono installare dei plug-in che permettano alle loro memorie cache di riconoscere l’estensione “immutable”;
- i server administrator devono aggiungere il token al campo Cache-Control dell’header Http di ogni risorsa che non sarà modificata per periodi di tempo molto lunghi.


## Prove Sperimentali
### Risorsa immutable
Per testare i vantaggi del token immutable sono stati misurati i tempi di risposta da parte del server relativi ad una risorsa immutabile. Questi test sono stati effettuati attraverso la rete Wi-Fi dell’Università di Verona con il browser Firefox48 (la versione precedente al rilascio del plug-in per riconoscere la nuova estensione) in modo da poter fare una media dei tempi che intercorrevano tra la richiesta “If-Modified-Since” da parte del client e la risposta “304 – NotModified” del server. \
*Il test si basa su 100 rilevazioni rappresentate in millisecondi.* \
\
La risorsa che si vuole analizzare è la scritta facebook della pagina principale del sito facebook.com, avente “max-age”=31536000 secondi (1 anno).  
URL della risorsa: https://www.facebook.com/rsrc.php/v3/y4/r/gf6iHxsw8zm.png *Media dei tempi: 23,76ms* \
\
Con l’implementazione del token immutable è possibile risparmiare completamente il tempo speso per inviare la richiesta di aggiornamento di questa risorsa.


##  Pagina con risorse immutable

Sono stati inoltre misurati i tempi di risposta relativi al caricamento dell’intera pagina principale di Facebook, avente risorse con “max-age”=31536000 secondi (1 anno). Il test si basa su 100 rilevazioni rappresentate in secondi, effettuate anch’esse su Firefox48. \
*URL della pagina: https://www.facebook.com Media dei tempi: 1,97s* \
\
Come si può notare attraversi i dati raccolti, ad ogni refresh condizionale si spende del tempo per verificare se la risorsa è ancora valida, nonostante si tratti di informazioni che resteranno tali per un periodo molto lungo (come indicato dal “max-age”). Implementando il token immutable è possibile risparmiare completamente il tempo speso per inviare molte richieste, abbassando notevolmente il tempo di caricamento dell’intera pagina. \
\
Per dimostrare questo miglioramento sono stati effettuati altri 100 test sullo stesso URL, utilizzando però la versione aggiornata di Firefox (che implementa appunto l’estensione sul Cache-Control). \
*Media dei tempi: 1,77s*


### Simulazione Client/Server
Per testare i vantaggi “lato client” e “lato server” sono stati riprodotti tramite linguaggio Java un Client ed un Server (in esecuzione su host diversi) in grado di interagire tra loro tramite la rete per simulare lo scambio di richieste per certe risorse. \
\
Ogni richiesta generata dal Client viene incapsulata in una sorta di pacchetto Http contenente i seguenti campi:
-  **\<timestamp\>**: tempo di arrivo della richiesta;
- **\<objID\>**: identificativo dell’oggetto;
- **\<size\>**: dimensione dell’oggetto (byte);
- **\<dati_inviati\>**: quantità di dati effettivamente serviti (byte);
- **\<header_only\>**: se viene settato a 1 indica che l’oggetto è stato richiesto con “If-Modified-Since” e la risposta dovrà essere “304–NotModified”;
- **\<is_fragment\>**: se viene settato a 1 indica che il pacchetto iniziale è stato frammentato (campo ignorato durante i test).

La richiesta viene poi inviata al server che, dopo aver analizzato i campi del pacchetto, genererà la risposta corrispondente. \
\
Nei primi test gli oggetti richiesti con “header_only=1” genereranno una risposta del tipo “304 – NotModified”. Successivamente è stato implementato il token immutable, evitando dunque di inviare tali richieste. \
Entrambi gli host implementano l’utilizzo delle thread, rendendo possibile l’invio e l’elaborazione di più richieste in parallelo. \
\
Le varie prove sono state avviate inizialmente su un file contenente 100 mila richieste e in seguito su altre 20 milioni. Al termine di ogni prova è stato salvato il rispettivo tempo di esecuzione, che ci ha permesso di constatare gli effettivi miglioramenti dovuti all’utilizzo dell’estensione sul Cache-Control. \
\
Ulteriori ottimizzazioni in termini di tempo sono dovute anche all’utilizzo di più thread che però non devono superare il numero di “core” del processore che ospita il programma in esecuzione, in quanto andrebbero ad incrementare il numero di Context Switch, tempo che va a sommarsi all’effettiva elaborazione delle richieste. \
I tempi possono essere ulteriormente falsati dalla presenza di altri programmi in esecuzione sullo stesso host, dal numero di “core” del processore, dall’eventuale traffico presente nella rete e dalla velocità di trasferimento di quest’ultima.