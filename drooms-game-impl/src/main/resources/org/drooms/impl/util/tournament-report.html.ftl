<!DOCTYPE html>
<html>
  <head>
    <title>Drooms: Results of tournament '${name}'</title>
    <meta charset='utf-8'>
  </head>
  
  <body>
    <header>
        <h1>Drooms: Results of tournament '${name}'</h1>
        <p>Find below results of the tournament and of particular games.</p>
        <table>
          <caption>Players in the tournament</caption>
          <tbody>
<#list players as player> 
          <tr><td>${player.getName()}</td></tr>
</#list>
          </tbody>
        </table>
        <table>
          <caption>Games played</caption>
          <thead>
            <tr><th>Playground</th><th>Played</th></tr>
          </thead>
          <tbody>
<#list games.entrySet() as entry> 
            <tr><td>${entry.key}</td><td>${entry.value}x</td></li>
</#list>
          </tbody>
        </table>
    </header>
    <section>
        <h2>Overall results</h2>
        <table>
          <thead>
            <tr><th>Position</th><th>Player</th><th>Score</th></tr>
          </thead>
          <tbody>
<#assign i = 1>
<#list results.entrySet() as entry> 
    <#assign j = i>
    <#list entry.getValue() as player> 
        <tr><td>${j}</td><td>${player.getName()}</td><td>${entry.key}</td></li>
        <#assign i = i + 1>
    </#list> 
</#list>
          </tbody>
        </table>
    </section>
<#list gameScore.entrySet() as entry>
    <#assign gameName = entry.key> 
    <#assign gameResult = gameResults.get(gameName)> 
    <section>
        <h2>Results of games on playground '${gameName}'</h2>
        <table>
          <thead>
            <tr><th>Position</th><th>Player</th><th>Median</th><th>Max</th><th>Min</th></tr>
          </thead>
          <tbody>
<#assign i = 1>
<#list entry.value as entry2> 
    <#assign j = i>
    <#list entry2 as player> 
        <#assign max = gameResult.getMax(player)> 
        <#assign min = gameResult.getMin(player)> 
        <#assign median = gameResult.getMedian(player)> 
        <tr><td>${j}</td><td>${player.getName()}</td><td>${median}</td><td>${max}</td><td>${min}</td></li>
        <#assign i = i + 1>
    </#list> 
</#list>
          </tbody>
        </table>
    </section>
</#list>
    <footer>
      <p>Congratulations to the winners, and better luck next time to others!</p>
    </footer>
  </body>
</html>