import * as cdk from "aws-cdk-lib";
import * as elasticbeanstalk from "aws-cdk-lib/aws-elasticbeanstalk";
import * as iam from "aws-cdk-lib/aws-iam";
import { Construct } from "constructs";
import s3assets = require("aws-cdk-lib/aws-s3-assets");
import { DynamoDBStack } from "./DynamoDBStack";
import { ToolsSlackBotStackProps } from "../../bin/aws-cdk";
import { TableName } from "../constants/Table";

interface ToolsSlackBotEbStackProps extends ToolsSlackBotStackProps {
  dynamoDBStack: DynamoDBStack;
}
export class ToolsSlackEbBotStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: ToolsSlackBotEbStackProps) {
    super(scope, id, props);
    const { stage, appName, dynamoDBStack } = props;

    const EbInstanceRole = new iam.Role(
      this,
      `${appName}-eb-ec2-role-${stage}`,
      {
        assumedBy: new iam.ServicePrincipal("ec2.amazonaws.com"),
      }
    );

    dynamoDBStack
      .getTable(TableName.ACCOUNT)
      .grantReadWriteData(EbInstanceRole);
    dynamoDBStack
      .getTable(TableName.BOOKING)
      .grantReadWriteData(EbInstanceRole);

    const managedPolicy = iam.ManagedPolicy.fromAwsManagedPolicyName(
      "AWSElasticBeanstalkWebTier"
    );
    EbInstanceRole.addManagedPolicy(managedPolicy);

    const profileName = `${appName}-EB-InstanceProfile-${stage}`;
    const instanceProfile = new iam.CfnInstanceProfile(this, profileName, {
      instanceProfileName: profileName,
      roles: [EbInstanceRole.roleName],
    });

    // EBS Application and Environment
    const app = new elasticbeanstalk.CfnApplication(this, "Application", {
      applicationName: `${appName}-${stage}`,
    });

    const webAppZipArchive = new s3assets.Asset(this, "ToolsBotWebZip", {
      path: `${__dirname}/../../../build/libs/slack-zoom-0.0.1-SNAPSHOT.jar`,
    });

    const appVersionProps = new elasticbeanstalk.CfnApplicationVersion(
      this,
      "AppVersion",
      {
        applicationName: app.applicationName!!,
        sourceBundle: {
          s3Bucket: webAppZipArchive.s3BucketName,
          s3Key: webAppZipArchive.s3ObjectKey,
        },
      }
    );
    appVersionProps.addDependency(app);

    interface Env {
      key: string;
      value: string;
    }

    const environmentVaribles: Env[] = [
      {
        key: "DB_ACCOUNTS_TABLE_NAME",
        value: dynamoDBStack.getTable(TableName.ACCOUNT).tableName,
      },
      {
        key: "DB_BOOKINGS_TABLE_NAME",
        value: dynamoDBStack.getTable(TableName.BOOKING).tableName,
      },
      {
        key: "SLACK_BOT_TOKEN",
        value: "xoxb-4623273785473-4625409455686-H9LFqmolAoXgfVgQCsMGMInR",
      },
      {
        key: "SLACK_SIGNING_SECRET",
        value: "d6dbd595e1800fcf47099aaa61e827d2",
      },
      {
        key: "BOT_UPDATE_CHANNEL_ID",
        value: "C04HS17S0JJ",
      },
      {
        key: "SERVER_PORT",
        value: "5000",
      },
    ];

    const buidOptionsFromEnv =
      (): elasticbeanstalk.CfnEnvironment.OptionSettingProperty[] => {
        return Object.values(environmentVaribles).map((env) => {
          return {
            namespace: "aws:elasticbeanstalk:application:environment",
            optionName: env.key,
            value: env.value,
          };
        });
      };

    const optionSettingProperties: elasticbeanstalk.CfnEnvironment.OptionSettingProperty[] =
      [
        {
          namespace: "aws:autoscaling:launchconfiguration",
          optionName: "IamInstanceProfile",
          value: instanceProfile.instanceProfileName,
        },
        {
          namespace: "aws:autoscaling:asg",
          optionName: "MinSize",
          value: "1",
        },
        {
          namespace: "aws:autoscaling:asg",
          optionName: "MaxSize",
          value: "1",
        },
        {
          namespace: "aws:ec2:instances",
          optionName: "InstanceTypes",
          value: "t2.micro",
        },
        {
          namespace: "aws:elb:healthcheck",
          optionName: "Target",
          value: "/health",
        },
        ...buidOptionsFromEnv(),
      ];

    const elbEnv = new elasticbeanstalk.CfnEnvironment(this, "Environment", {
      environmentName: `${appName}-${stage}-env`,
      applicationName: app.applicationName || appName,
      solutionStackName: "64bit Amazon Linux 2 v3.4.3 running Corretto 17",
      optionSettings: optionSettingProperties,
      versionLabel: appVersionProps.ref,
    });

    elbEnv.addDependency(app);
  }
}
