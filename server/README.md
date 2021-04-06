# README #

### Tutorial ###

Para obter os lugares próximos. 
Mandar um POST para:
https://great-room.appspot.com/api/groups/nearby

```
#!json
{
    "locations": [
        {
            "type": "ibeacon",
            "uuid": "00000000-0000-0000-0000-000000000000",
            "distance"=>1.5
        }
    ]
}	
```
Resposta com sucesso
```
#!json
[{"id":"1","name":"teste","description":"lugar de teste","image_url":null,"creation_date":"2015-10-04 02:06:47"}]
```
Resposta com erro
```
#!json
{"success":false,"code":1,"message":"Dados inv\u00e1lidos"}
```

- - -

Para fazer um check-in.
Mandar um POST para:
https://great-room.appspot.com/api/group/checkin

```
#!json
{
    "objects": {
        "email": "adyson.maia@gmail.com",
        "name": "Adyson M. Maia",
        "image_url": ""
    },
    "group": {
        "id": 1
    }
}
```

- - -

Para obter a lista de objetos atuais do grupo
Mandar um POST para:
https://great-room.appspot.com/api/group/objects/current

```
#!json
{
    "group": {
        "id": 1
    },
    "type": "person"
}
```
Resposta com sucesso
```
#!json
{
    "success": true,
    "response": [
        {
            "object_id": "1",
            "email": "adyson.maia@gmail.com",
            "name": "Adyson M. Maia",
            "image_url": "",
            "id": "1",
            "uuid": "",
            "type": "PERSON",
            "creation_date": "2015-10-21 23:49:42",
            "checkin_date": "Mon, 07 Dec 2015 14:40:18 -0300"
        }
    ]
}
```

