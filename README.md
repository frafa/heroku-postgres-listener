# heroku-postgres-listener

Classe di test che riceve la notifica sull'inserimento di un record in una tabella postgres su Heroku.

Requisiti
---------

- Account su Heroku
- Creare un database Postgres su Heroku

Inizializzazione del database
-----------------------------

Per la creazione della tabella utilizzare lo script

```
-- Se presente procedere a cancellare la tabella
DROP TABLE public.test_table

-- Creazione della tabella
CREATE TABLE public.test_table (
	"key" varchar(64) NOT NULL,
	value int4 NOT NULL
);
```

Per la generazione della store procedure che si occupa di generare la *notify* e del trigger ad essa collegata.

```
CREATE OR REPLACE FUNCTION PUBLIC.NOTIFY() RETURNS trigger AS
$BODY$
BEGIN
    PERFORM pg_notify('insert_event', row_to_json(NEW)::text);
    RETURN new;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE COST 100;


CREATE TRIGGER test_table_AFTER
AFTER INSERT
ON PUBLIC.test_table
FOR EACH ROW
EXECUTE PROCEDURE PUBLIC.NOTIFY();
```

Avvio dell'applicazione
-----------------------

### Deploy
Per deployare l'applicazione occorre effettuare le seguenti attività:
- collegare il progetto GitHub a Heroku 
- procedere a deployare manualmente il progetto

### Start/Stop del dyno
L'applicazione viene eseguita come *worker* (come specificato nel file *Procfile* presente nella root) occorre pertanto avviare e stoppare il dyno da riga di comando

Start
```
heroku ps:scale worker=1 -a <nome-app>
```

Stop

```
heroku ps:scale worker=0 -a <nome-app>
```



Test dell'applicazione
----------------------

Una volta avviata l'applicazione ad ogni inserimento di un record sulla tabella viene scritto una riga sul log con il dettaglio dell'evento generato.

```
INSERT INTO public.test_table ("key", value) VALUES('uno', 1);
```

log:
```
Got notification: insert_event{"key":"uno","value":1}
```

Per visualizzare i log occorre selezionare la voce *View logs* dal menù *More*.
