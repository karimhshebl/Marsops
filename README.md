# MarsOps

Mars colony simulator for Programming II, Lab 5 (Alexandria University, Faculty of Engineering, Fall 2026).

The program models a small colony with crew members, robots, facilities, shared resources, missions, emergencies and a changing environment (dust storms and communication disruptions). Time moves in whole time units.

## Requirements

JDK 17 or newer (compiled and tested with JDK 21). No libraries are needed. The GUI uses Swing, which comes with the JDK.

## Build and run

From the project root:

```
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```

On Linux or macOS use `javac -d out $(find src -name "*.java")` for the first line.

`Main` opens the GUI. To run all demonstration scenarios in the console, pass the argument `console`:

```
java -cp out Main console
```

In IntelliJ the argument goes under Run, Edit Configurations, Program arguments.

## Scenarios

Each scenario builds its own colony.

1. Successful exploration: a scientist and a rover with a soil analyzer complete a 3 unit exploration mission.
2. Mission rejection: an engineer cannot do exploration, so the mission fails with a reason.
3. Resource shortage: only 5 fuel exists but the mission needs 10, so it is rejected and nothing is consumed.
4a. Oxygen leak, resolved: an engineer and a repair robot respond, resources are consumed and the habitat is repaired.
4b. Oxygen leak, unresolved: no repair robot is available, so the emergency is unresolved and the oxygen loss is applied a second time.
5a. A mission submitted during a dust storm is rejected.
5b. A dust storm starts while a mission is running and the mission fails.
5c. A communication disruption makes a drone unusable until conditions are normal again.
6. Extra case: a rover breaks down during a mission and there is nobody to repair it.

## Project layout

Everything is under `src`. Folder names must match the package names exactly (`Missions`, `universalHelpers`).

- `Main`: entry point.
- `colony`: `MarsColony`, `SurfaceLocation`.
- `simulation`: `Simulation` (the clock, scheduled environment changes and emergencies).
- `agents`: `Agent`, `HumanPersonnel`, `RoboticUnit` and the concrete types (`Scientist`, `Engineer`, `MedicalOfficer`, `ExplorationRover`, `RepairRobot`, `Drone`).
- `assets.facilities`: `Facility` and its five subclasses (`Habitat`, `Laboratory`, `PowerStation`, `StorageFacility`, `CommunicationStation`), plus `EntityManager`.
- `assets.resources`: inventory, reservations, `ResourceManager` and the two allocation policies.
- `assets.equipment`: `Equipment`.
- `Missions`: `Mission`, its four subclasses and `MissionManager`.
- `emergency`: `Emergency`, its four subclasses, `ResponderRequirement` and `EmergencyManager`.
- `interfaces`, `enums`, `universalHelpers`: shared interfaces, enums, `IdGenerator` and `Validation`.
- `scenario`: the scenarios used by both `Main` and the GUI.
- `gui`: the Swing window and its panels.

## How it works

Agents. Every agent has an id, a name, a location, an availability flag and a set of capabilities. A person is usable when health is at least 50 and fatigue is at most 50. A robot is usable when its battery is above 30 and it is not damaged. Each time unit an agent on a mission gets more tired (people, +10 fatigue) or loses battery (robots, -10), and an idle agent recovers. Drones also stop being usable during a communication disruption.

Resources. Facilities keep their own stock and register it in the colony's `ResourceInventory`. A request first reserves what it needs through `ResourceManager`, which asks the allocation policy how much each priority may take. The reservation is committed (stock taken) when the mission starts or the emergency is handled, and cancelled if it fails. `TieredAllocationPolicy` keeps life support stock for life critical requests and limits routine requests when supplies run low.

Missions. A mission goes PENDING, ASSIGNED, IN_PROGRESS and then COMPLETED, or FAILED at any point with a reason. Only one mission can be active at a time. `MissionManager.submit` checks the active mission, the agents, the environment and the resources in that order. A rejected mission is FAILED immediately. A failed mission is never retried.

Emergencies. `EmergencyManager.handle` applies the incident, checks that enough responders are available, reserves and commits the response cost, moves the responders to the location and then resolves the emergency. If responders or resources are missing, the emergency is UNRESOLVED with a reason and its penalty is applied. Emergencies do not interrupt the active mission.

Simulation. In each time unit `Simulation` applies a scheduled environment change, calls `MarsColony.tick()` (agents, supplies, then the active mission), logs finished missions and triggers scheduled emergencies.

## Using the GUI

`Main` opens a window with a control bar and seven tabs. The event log at the bottom shows everything that happens.

- Control bar: advance time by N units, set the environment now (`NORMAL`, `DUST_STORM`, `COMMUNICATION_DISRUPTION`) or schedule a change for a later time unit.
- Overview: the full colony status report (resources, facilities, agents, missions).
- Agents: add crew and robots, see their state.
- Facilities: add facilities with starting stock.
- Resources: stock, reserved amounts and consumption per time unit.
- Missions: choose a type, duration, target facilities and agents, then submit. Advance time to run it.
- Emergencies: trigger an oxygen leak, fire, medical emergency or rover breakdown.
- Scenarios: run one scenario or all of them. Each scenario replaces the current colony.

## Missions

| Mission | Resources reserved | Main rules |
|---|---|---|
| Exploration | 10 fuel, 15 battery power | Needs an agent with surface exploration and a vehicle carrying a soil analyzer. Rejected in a dust storm or a communication disruption. |
| Repair | 2 spare parts, 5 battery power | Needs a human and a robot that both have repair capability (an engineer and a repair robot). |
| Rescue | 15 fuel, 10 oxygen, 2 medical supply | Life critical priority. Needs a surface-capable robot as vehicle, a human, and an agent with communication capability. Rejected in a dust storm or a communication disruption. |
| Research | 10 oxygen, 20 battery power | Needs a laboratory with laboratory apparatus and a researcher. |

All missions are routine priority except rescue. A mission that is running when a dust storm starts fails.

## Emergencies

| Emergency | Responders (people, robots) | Response cost | Effect |
|---|---|---|---|
| Oxygen leak | 1 person, 1 robot | 2 spare parts, 5 battery power | The habitat loses oxygen and is damaged. It is repaired when resolved, and loses oxygen again if unresolved. |
| Fire | 1 person | 10 water, 5 battery power | The laboratory becomes unavailable, its equipment is damaged, oxygen use rises and people inside lose health. Resolved restores the laboratory, unresolved destroys it. |
| Medical | 1 person | 2 medical supply | The patient loses health, recovers when resolved and loses more if unresolved. |
| Rover breakdown | 1 person, 1 robot | 2 spare parts, 5 battery power | The rover is damaged and is repaired when resolved. |

## Allocation policy

`TieredAllocationPolicy` decides how much of a resource a request may take. Life support stock (oxygen, water, food) keeps a reserve of 48 time units of consumption for everything except life critical requests. Routine requests cannot use the 30% of spare parts and medical supply held back for emergencies. For routine requests, a resource that will last more than 72 time units is NORMAL, between 24 and 72 is CONSERVE (half of the free stock) and under 24 is CRITICAL (nothing). `FirstComeFirstServedPolicy` is the simpler alternative policy.

## Assumptions and limits

- Only one mission can be active at a time.
- Emergencies are handled as soon as they are triggered. Responders are moved to the location but are not kept busy afterwards.
- Dispatching an agent changes its location but does not edit the personnel list of the facility it left.
- `MarsColony.submitMission` does not check that the agents were added to the colony.
- Resources used by a mission are not returned if the mission fails while running.

## UML diagrams

The diagrams are in the `UML` folder:

- `MarsOps-master-class-diagram_1.drawio` (editable source) and its `.drawio.png` export: the master class diagram with every class.
- `07-object-diagram.png`: object diagram of a running colony.
- `08-state-machine-mission.png`: mission state machine.
- `09-sequence-mission.png`, `10-sequence-oxygen-leak.png`, `11-sequence-dust-storm.png`: sequence diagrams.
- `12-use-case.png`: use case diagram.
