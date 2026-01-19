Feature: Trade settlement processing
  Settlement Service finalizes a trade by calculating monetary values
  and marking the trade as SETTLED.
  It assumes all trades are already validated by upstream services.

  Background:
    Given a valid trade with required fields populated

  # Happy path

  Scenario: Successfully settle a valid trade
    Given the trade has quantity 100 and price 50
    When the trade is settled
    Then the gross amount should be quantity multiplied by price
    And the commission should be 0.3 percent of the gross amount
    And the tax should be 0.5 percent of the gross amount
    And the net amount should be gross minus commission and tax
    And the trade status should be "SETTLED"
    And the broker id should be assigned
    And the received time should be populated

  # Business rules & assumptions

  Scenario: Settlement assumes trade is already validated
    Given the trade has passed validation in upstream services
    When the trade is settled
    Then the settlement processor should not perform validation checks
    And the trade should be settled successfully

  # Idempotency / safety behavior

  Scenario: Settling an already settled trade
    Given the trade status is "SETTLED"
    When settlement is triggered
    Then the settlement processor recalculates the trade
    And the database layer prevents overriding the existing settlement

  # Edge cases (documented behavior)

  Scenario: Settling a trade with zero quantity
    Given the trade has quantity 0 and price 50
    When the trade is settled
    Then the gross amount should be 0
    And the commission should be 0
    And the tax should be 0
    And the net amount should be 0
    And the trade status should be "SETTLED"

  Scenario: Settling a trade with zero price
    Given the trade has quantity 100 and price 0
    When the trade is settled
    Then the gross amount should be 0
    And the commission should be 0
    And the tax should be 0
    And the net amount should be 0
    And the trade status should be "SETTLED"

  # Calculation correctness

  Scenario Outline: Settlement calculation accuracy
    Given the trade has quantity <quantity> and price <price>
    When the trade is settled
    Then the gross amount should be <gross>
    And the commission should be <commission>
    And the tax should be <tax>
    And the net amount should be <net>

    Examples:
      | quantity | price | gross | commission | tax | net |
      | 10       | 100   | 1000  | 3.0        | 5.0 | 992 |
      | 5        | 200   | 1000  | 3.0        | 5.0 | 992 |
      | 1        | 1     | 1     | 0.003      | 0.005 | 0.992 |

  # Audit / traceability

  Scenario: Settlement populates audit fields
    Given the trade is ready for settlement
    When the trade is settled
    Then the received time should reflect the settlement time
    And the trade status should indicate final settlement
