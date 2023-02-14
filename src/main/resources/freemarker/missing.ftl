<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link rel="stylesheet" type="text/css" href="${cssPath}">
</head>

<body>

<div class="title">${dayName}, ${name}</div>

<div class="message">Menu pro tento den není k dispozici.</div>

<div class="links">
    <a href="/${admin?string('iqadmin', 'iq')}/${name}-prev">&lt;&lt; Předchozí</a>
    <a href="/${admin?string('iqadmin', 'iq')}/${name}-next">Následující &gt;&gt;</a>
    <a href="/${admin?string('iqadmin', 'iq')}">Dnes</a>
    <a href="${iqurl}">Aktuální Eatology pdf</a>
</div>

</body>
</html>