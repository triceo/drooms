<!DOCTYPE html>
<html>
  <head>
    <title>Drooms: Results of tournament '${name}'</title>
    <meta charset='utf-8'>
    <script src='http://html5slides.googlecode.com/svn/trunk/slides.js'></script>
  </head>
  
  <body style='display: none'>
    <section class='slides layout-regular template-default'>
      <article>
        <h1>Drooms: '${name}'</h1>
        <p>Results of the tournament and of particular games.</p>
      </article>
      <article>
        <h3>Players in the tournament</h3>
        <table>
          <tbody>
<#list players as player> 
          <tr><td>${player.getName()}</td></tr>
</#list>
          </tbody>
        </table>
      </article>
      <article>
        <h3>Games played</h3>
        <table>
          <thead>
            <tr><th>Playground</th><th>Played</th></tr>
          </thead>
          <tbody>
<#list games.entrySet() as entry> 
            <tr><td>${entry.key}</td><td>${entry.value}x</td></li>
</#list>
          </tbody>
        </table>
      </article>
      <article>
        <h3>Overall results</h3>
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
      </article>
<#list gameScore.entrySet() as entry>
    <#assign gameName = entry.key> 
    <#assign gameResult = gameResults.get(gameName)> 
      <article>
        <h3>Results of games on playground '${gameName}'</h3>
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
      </article>
</#list>
  </body>
</html>