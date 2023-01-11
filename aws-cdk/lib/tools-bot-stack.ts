import * as cdk from "aws-cdk-lib";
import { AttributeType, Table } from "aws-cdk-lib/aws-dynamodb";
import { Construct } from "constructs";
import { StageType } from "./Stage";

export interface ToolsSlackBotStackProps extends cdk.StackProps {
  stage: StageType;
}
export class ToolsSlackBotStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: ToolsSlackBotStackProps) {
    super(scope, id, props);

    const accountsTable = new Table(
      this,
      `slack-tools-bot-accounts-${props.stage}`,
      {
        partitionKey: {
          name: "accountId",
          type: AttributeType.STRING,
        },
        tableName: `slack-tools-bot-accounts-${props.stage}`,
      }
    );

    const bookingsTable = new Table(
      this,
      `slack-tools-bot-bookings-${props.stage}`,
      {
        partitionKey: {
          name: "bookingId",
          type: AttributeType.STRING,
        },
        tableName: `slack-tools-bot-bookings-${props.stage}`,
      }
    );
  }
}
