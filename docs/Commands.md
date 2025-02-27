# Commands

Commands in Commander are the main actors in your subscription.

In JSON, a command is an object. The required `type` field selects the command that will be executed when the event is invoked.

All command types accept a special `condition` field, that can operate on the current event context. The field uses the vanilla predicate system, so [misode.github.io](https://misode.github.io/predicate/) can be used to quickly create predicates.

Various command types can take additional parameters.

You can also use Commander commands in advancement rewards:

```json
{
  "rewards": {
    "commander:commands": [
      {
        "type": "commander:print",
        "text": "42"
      }
    ]
  }
}
```

And other projects might integrate commands to other contexts!

[[toc]]

## Selectors

Some commands require you to specify a "selector". A selector is a simple identifier that allows you to select a position and (optionally) an entity that will be used to perform an action. Selectors, with some exceptions, mimic vanilla loot contexts, built-in selectors can be found here: [BuiltInSelectors](https://github.com/constellation-mc/commander/blob/main/src/main/java/me/melontini/commander/impl/builtin/BuiltInSelectors.java)

## Built-in commands
The mod comes with a minimal set of commands, this is by design, as game interactions should be handled by brigadier `/` commands, or functions.

### `commander:commands`
This is the main type of command you will use when writing subscriptions.

This command type expects a selector and either a list of commands or a function identifier. It's recommended that you provide a function identifier, as these are verified by the function loader on reload.

::: details Example
```json
{
  "type": "commander:commands",
  "selector": "commander:random_player",
  "commands": [
    "/cmd:explode ~ ~1 ~ 2"
  ]
}
```
:::

#### Command Macros

If you opt into specifying commands using the array, you are a given an option of using command macros.

A macro is a simple `${{}}` block, which allows you to dynamically insert info using expressions. For example:
```
"/say hi, my name is ${{level.dimension.location}}!"
```
will say `hi, my name is minecraft:overworld` in the overworld.

You can learn more about expressions on this page: [Expressions](Expressions).

By default macros return a string, but you can cast (convert) a macro into some other types like so:

```
$(long){{random(0, 34)}} 34.52946 -> 34
$(double){{true}} true -> 1.0
$(bool){{0}} 0 -> false
```

### `commander:cancel`
This is a special command type that can only be used with supported event types.

The only field required by this command is `value` with an appropriate return value.

::: details Example
```json
{
  "type": "commander:cancel",
  "value": false
}
```

```json
{
  "type": "commander:cancel",
  "value": "fail"
}
```
:::

### `commander:all_of`, `commander:any_of`, `commander:defaulted`
All three of these commands have a similar function. They accept a list of commands and if the condition is true, they execute the command specified in the optional `then` block.

`all_of` requires all commands to execute successfully, `any_of` requires one command to execute successfully, and `defaulted` requires one command to fail.

Note: even if the condition is true early, the command will still execute all commands! To chanage this, you can set `short_circuit` to true, in which case commands will be aborted immediately if the condition fails.

::: details Example
```json
{
  "event": "commander:player_use/item",
  "commands": [
    {
      "type": "commander:all_of",
      "condition": {
        "condition": "minecraft:match_tool",
        "predicate": {
          "items": [
            "minecraft:diamond"
          ]
        }
      },
      "commands": [
        {
          "type": "commander:commands",
          "selector": "this_entity",
          "commands": [
            "/say mmm... diamond..."
          ]
        },
        {
          "type": "commander:cancel",
          "value": "success"
        }
      ]
    }
  ]
}
```
:::

### `commander:random`
This command type takes a weighted list of other commands and randomly executes some of them. By default, it rolls only once, but the number of rolls can be adjusted using the optional `rolls` field (Supports [Expressions](Expressions)).

::: details Example
```json
{
  "type": "commander:random",
  "rolls": 2,
  "commands": [
    {
      "weight": 2,
      "data": {
        "type": "commander:commands",
        "selector": "this_entity",
        "commands": [
          "/say weight 2"
        ]
      }
    },
    {
      "weight": 6,
      "data": {
        "type": "commander:commands",
        "selector": "this_entity",
        "commands": [
          "/say weight 6"
        ]
      }
    }
  ]
}
```
:::
