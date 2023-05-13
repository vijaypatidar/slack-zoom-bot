import * as cdk from "aws-cdk-lib";
import { AttributeType, BillingMode, Table } from "aws-cdk-lib/aws-dynamodb";
import { Construct } from "constructs";
import { ToolsSlackBotStackProps } from "../../bin/aws-cdk";
import { TableName } from "../constants/Table";

export interface DynamoDBStackProps extends ToolsSlackBotStackProps {}

export class DynamoDBStack extends cdk.Stack {
  private readonly tables: Record<TableName, Table>;

  constructor(scope: Construct, id: string, props: DynamoDBStackProps) {
    super(scope, id, props);
    const { stage } = props;

    const accountsTable = new Table(this, `slack-tools-bot-accounts-${stage}`, {
      partitionKey: {
        name: "accountId",
        type: AttributeType.STRING,
      },
      tableName: `slack-tools-bot-accounts-${stage}`,
      billingMode: BillingMode.PAY_PER_REQUEST,
    });

    const bookingsTable = new Table(this, `slack-tools-bot-bookings-${stage}`, {
      partitionKey: {
        name: "bookingId",
        type: AttributeType.STRING,
      },
      tableName: `slack-tools-bot-bookings-${stage}`,
      billingMode: BillingMode.PAY_PER_REQUEST,
    });

    accountsTable.applyRemovalPolicy(cdk.RemovalPolicy.DESTROY);
    bookingsTable.applyRemovalPolicy(cdk.RemovalPolicy.DESTROY);

    this.tables = {
      [TableName.ACCOUNT]: accountsTable,
      [TableName.BOOKING]: bookingsTable,
    };
  }

  getTable(table: TableName) {
    return this.tables[table];
  }
}
