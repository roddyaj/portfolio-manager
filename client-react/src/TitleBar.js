function TitleBar(props) {
	const { accounts, setSelectedAccount, showAllPositions, setShowAllPositions } = props;

	const accountOptions = accounts.map((account) => {
		return (<option key={account} value={account}>{account}</option>);
	});

	return (
		<div className="pm-block">
			<div style={{ display: "flex", alignItems: "center" }}>
				<div style={{ marginRight: 12 }}>
					<select name="accounts" id="accounts" onChange={(e) => setSelectedAccount(e.target.value)}>
						{accountOptions}
					</select>
				</div>

				<div style={{ marginRight: 6 }}>
					<span style={{ marginRight: 6 }}>Positions:</span>
					<input type="radio" style={{ marginRight: 2 }} name="positionVisibility" id="visibility-all" value="all" checked={showAllPositions} onChange={() => setShowAllPositions(true)} />
					<label htmlFor="visibility-all" style={{ marginRight: 6 }}>All</label>
					<input type="radio" style={{ marginRight: 2 }} name="positionVisibility" id="visibility-actions" value="actions" checked={!showAllPositions} onChange={() => setShowAllPositions(false)} />
					<label htmlFor="visibility-actions" style={{ marginRight: 6 }}>With actions</label>
				</div>
			</div>
		</div>
	);
}

export default TitleBar;
